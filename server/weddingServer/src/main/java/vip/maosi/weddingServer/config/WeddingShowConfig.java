package vip.maosi.weddingServer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * @Author liudw
 * @Date 2025/4/25 11:33
 * @Version v1.0
 */
@Component
@ConfigurationProperties(prefix = "weddingshow")
public class WeddingShowConfig {
    /** 上传路径 */
    @NestedConfigurationProperty
    public static String profile;

    private Local local;

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public static class Local {
        private String address;
        private String storagePath;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getStoragePath() {
            return storagePath;
        }

        public void setStoragePath(String storagePath) {
            this.storagePath = storagePath;
        }
    }

    public static String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        WeddingShowConfig.profile = profile;
    }

    /**
     * 获取下载路径
     */
    public static String getDownloadPath()
    {
        return profile + "/download/";
    }

    /**
     * 获取上传路径
     */
    public static String getUploadPath()
    {
        return profile + "/upload";
    }
}
