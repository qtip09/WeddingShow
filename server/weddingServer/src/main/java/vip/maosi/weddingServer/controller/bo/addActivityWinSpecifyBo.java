package vip.maosi.weddingServer.controller.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author liudw
 * @Date 2025/5/29 16:24
 * @Version v1.0
 */
@Data
public class addActivityWinSpecifyBo {
    @NotNull(message = "不能为null")
    private Integer prizeId;
    @NotNull(message = "不能为null")
    private Integer uid;
    @NotBlank(message = "不能为null")
    private String name;
}
