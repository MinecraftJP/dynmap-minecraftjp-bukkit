package jp.minecraft.dynmap.minecraftjp.oauth;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * Created by ayu on 2016/02/16.
 */
@Data
public class UserInfo {
    @SerializedName("preferred_username")
    private String preferredUsername;
    private String profile;
    private String uuid;
    private String sub;
}
