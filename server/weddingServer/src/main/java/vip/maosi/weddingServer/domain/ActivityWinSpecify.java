package vip.maosi.weddingServer.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @Author liudw
 * @Date 2025/5/29 14:27
 * @Version v1.0
 */
@Data
@TableName("activity_win_specify")
public class ActivityWinSpecify {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer activityId;
    private Integer activityPrizeId;
    private Integer uid;
    private Date createDate;
    private Date updateDate;
    private Boolean deleted;
}
