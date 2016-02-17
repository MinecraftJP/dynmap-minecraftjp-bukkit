package jp.minecraft.dynmap.minecraftjp.oauth;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Date;

/**
 * Created by ayu on 2016/02/16.
 */
@Data
public class AccessToken {
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("expires_in")
    private int expiresIn;
    @SerializedName("token_type")
    private String tokenType;
    @SerializedName("scope")
    private String scope;
    @SerializedName("id_token")
    private String idToken;
    private Date issue = new Date();
}
