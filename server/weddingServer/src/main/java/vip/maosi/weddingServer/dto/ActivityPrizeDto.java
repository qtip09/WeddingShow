package vip.maosi.weddingServer.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @Author liudw
 * @Date 2025/4/27 10:02
 * @Version v1.0
 */
@Data
public class ActivityPrizeDto {
    private Integer id;
    private String prizeName;
    private Integer count;
    private Integer activityId;
    private String order;
    private Integer relId;
    private Integer fileId;
    private String fileName;
    private String filePath;
    private String fileType;
}
