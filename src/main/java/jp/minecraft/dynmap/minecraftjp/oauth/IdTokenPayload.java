package jp.minecraft.dynmap.minecraftjp.oauth;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ayu on 2016/02/16.
 */
public class IdTokenPayload {
    private String iss;
    private String sub;
    private String aud;
    private long iat;
    private long exp;
    @SerializedName("auth_time")
    private long authTime;
}
