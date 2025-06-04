package vip.maosi.weddingServer.dto;

import lombok.Data;

import java.util.Date;

/**
 * @Author liudw
 * @Date 2025/5/29 14:33
 * @Version v1.0
 */
@Data
public class ActivityWinSpecifyDto {
    private Integer id;
    private Integer activityId;
    private Integer activityPrizeId;
    private String prizeName;
    private Integer count;
    private Integer uid;
    private String openId;
    private String nickName;
    private Date createDate;
    private Date updateDate;
    private Boolean deleted;
}
