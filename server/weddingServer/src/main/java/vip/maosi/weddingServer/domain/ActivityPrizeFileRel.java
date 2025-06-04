package vip.maosi.weddingServer.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @Author liudw
 * @Date 2025/4/27 10:07
 * @Version v1.0
 */
@Data
@TableName("activity_prize_file_rel")
public class ActivityPrizeFileRel {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer activityPrizeId;
    private Integer fileId;
    private Date createDate;
    private Date updateDate;
    private Boolean deleted;
}
