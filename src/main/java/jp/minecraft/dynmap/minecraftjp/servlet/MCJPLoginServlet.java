package jp.minecraft.dynmap.minecraftjp.servlet;

import jp.minecraft.dynmap.minecraftjp.DynmapMinecraftJPPlugin;
import jp.minecraft.dynmap.minecraftjp.oauth.AccessToken;
import jp.minecraft.dynmap.minecraftjp.oauth.IdTokenPayload;
import jp.minecraft.dynmap.minecraftjp.oauth.UserInfo;
import jp.minecraft.dynmap.minecraftjp.util.HTTPRequest;
import jp.minecraft.dynmap.minecraftjp.util.Jwt;
import jp.minecraft.dynmap.minecraftjp.util.StringUtil;
import lombok.RequiredArgsConstructor;
import org.bukkit.OfflinePlayer;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

/**
 * Created by ayu on 2016/02/15.
 */
@RequiredArgsConstructor
public class MCJPLoginServlet extends HttpServlet {
    private final DynmapMinecraftJPPlugin plugin;

    private final static String BASE_URL = "https://minecraft.jp";
    private final static String USERID_ATTR = "userid";
    private final static String NONCE_ATTR = "minecraftjp_nonce";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // エラー表示
        String error = req.getParameter("error");
        if (error != null) {
            String errorDescription = req.getParameter("error_description");
            String message = errorDescription != null ? StringUtil.escapeHTML(errorDescription) : "Authorization failed. please try again later.";
            sendError(res, message);
            return;
        }

        HttpSession sess = req.getSession(true);
        if(sess.isNew()) {
            sess.setMaxInactiveInterval(60);
        }

        String code = req.getParameter("code");
        if (code != null) {
            try {
                AccessToken accessToken = getAccessToken(code);
                String nonce = (String) sess.getAttribute(NONCE_ATTR);
                try {
                    validateIdToken(accessToken.getIdToken(), nonce);
                } catch (Exception e) {
                    sendError(res, "Authorization failed. please try again later. (" + e.getMessage() + ")");
                    e.printStackTrace();
                    return;
                }
                UserInfo userInfo = getUserInfo(accessToken);

                // プレイヤーチェック
                if (plugin.getConfig().getBoolean("checkPlayerKnown", true) && !checkPlayerKnown(userInfo.getUuid())) {
                    sendError(res, "Please login to the server.");
                    return;
                }

                sess.setAttribute(USERID_ATTR, userInfo.getPreferredUsername());

                res.sendRedirect("/");
            } catch (IOException e) {
                sendError(res, "Authorization failed. please try again later.");
                return;
            }
        } else {
            String clientId = getClientId();
            String clientSecret = getClientSecret();
            String redirectUri = getRedirectUri();

            if (clientId.isEmpty() || clientSecret.isEmpty() || redirectUri.isEmpty()) {
                sendError(res, "Client ID or Client Secret or RedirectURI has not been configured. please check the configuration file (plugins/DynmapMinecraftJP/config.yml).");
                return;
            }

            String nonce = StringUtil.random(32);
            sess.setAttribute(NONCE_ATTR, nonce);

            res.sendRedirect(BASE_URL + "/oauth/authorize?response_type=code&client_id=" + clientId + "&scope=openid+profile&redirect_uri=" + redirectUri + "&nonce=" + nonce);
        }
    }

    /**
     * エラー表示
     *
     * @param res
     * @param message
     * @throws IOException
     */
    protected void sendError(HttpServletResponse res, String message) throws IOException {
        res.setContentType("text/html; charset=utf8");

        PrintWriter out = res.getWriter();
        out.println("<html><head><title>" + message + "</title></head>");
        out.println("<body bgcolor=\"#ffffff\">");
        out.println("<center><h1>" + message + "</h1></center>");
        out.println("<hr/>");
        out.println("<form action=\"\" method=\"get\">");
        out.println("<button type=\"submit\">Retry</button>");
        out.println("</form>");
        out.println("</body></html>");
        out.flush();
    }

    /**
     * アクセストークン取得
     *
     * @param code Authorization Code
     * @return AccessToken
     * @throws IOException
     */
    private AccessToken getAccessToken(String code) throws IOException {
        HTTPRequest req = HTTPRequest.post(BASE_URL + "/oauth/token");
        req.addPostParameter("client_id", getClientId());
        req.addPostParameter("client_secret", getClientSecret());
        req.addPostParameter("redirect_uri", getRedirectUri());
        req.addPostParameter("grant_type", "authorization_code");
        req.addPostParameter("code", code);
        String responseBody = req.send();

        return plugin.getGson().fromJson(responseBody, AccessToken.class);
    }

    /**
     * UserInfo取得
     *
     * @param accessToken AccessToken
     * @return UserInfo
     * @throws IOException
     */
    private UserInfo getUserInfo(AccessToken accessToken) throws IOException {
        HTTPRequest req = HTTPRequest.get(BASE_URL + "/oauth/userinfo");
        req.addHeader("Authorization", "Bearer " + accessToken.getAccessToken());
        String responseBody = req.send();

        return plugin.getGson().fromJson(responseBody, UserInfo.class);
    }

    /**
     * IDTokenの検証
     *
     * @param idTokenStr
     * @param nonce
     */
    private void validateIdToken(String idTokenStr, String nonce) {
        JSONObject payload = Jwt.decode(idTokenStr, plugin.getCertificate());
        String aud = (String) payload.get("aud");
        if (!getClientId().equals(aud)) {
            throw new IllegalArgumentException("Client ID mismatch");
        }

        String iss = (String) payload.get("iss");
        if (!"minecraft.jp".equals(iss)) {
            throw new IllegalArgumentException("issuer mismatch");
        }

        String n = (String) payload.get("nonce");
        if (nonce != null && !nonce.equals(n)) {
            throw new IllegalArgumentException("nonce mismatch");
        }
    }

    /**
     * プレイヤーのログインチェック
     *
     * @param uuid
     * @return
     */
    private boolean checkPlayerKnown(String uuid) {
        UUID u = StringUtil.toUUID(uuid);
        OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(u);
        return offlinePlayer.hasPlayedBefore();
    }

    /**
     * ClientID取得
     *
     * @return ClientId
     */
    private String getClientId() {
        return plugin.getConfig().getString("clientId", "");
    }

    /**
     * ClientSecret取得
     *
     * @return ClientSecret
     */
    private String getClientSecret() {
        return plugin.getConfig().getString("clientSecret", "");
    }

    /**
     * RedirectURI取得
     *
     * @return RedirectURI
     */
    private String getRedirectUri() {
        return plugin.getConfig().getString("redirectUri", "http://127.0.0.1:8123/up/minecraftjp/login");
    }
}
