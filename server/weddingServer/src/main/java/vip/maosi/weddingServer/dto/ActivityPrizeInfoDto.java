package vip.maosi.weddingServer.dto;

import lombok.Data;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @Author liudw
 * @Date 2025/4/27 09:57
 * @Version v1.0
 */
@Data
public class ActivityPrizeInfoDto {
    private List<ActivityPrizeDto> activityPrizeDtoList;
}
