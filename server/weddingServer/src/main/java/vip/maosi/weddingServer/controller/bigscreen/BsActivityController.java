package vip.maosi.weddingServer.controller.bigscreen;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vip.maosi.weddingServer.domain.User;
import vip.maosi.weddingServer.dto.ActivityPrizeInfoDto;
import vip.maosi.weddingServer.dto.ActivityWinUser;
import vip.maosi.weddingServer.response.RGenerator;
import vip.maosi.weddingServer.response.ResEntity;
import vip.maosi.weddingServer.service.ActivityService;

import java.util.List;

/**
 * @Author liudw
 * @Date 2025/6/13 15:56
 * @Version v1.0
 */
@Validated
@RestController
@RequestMapping("/bigscreen")
public class BsActivityController {

    @Autowired
    ActivityService activityService;

    @GetMapping("/getActivityPrizeList")
    public ResEntity<ActivityPrizeInfoDto> getActivityPrizeList(@RequestParam @NotBlank(message = "code不能为空") String code) {
        val activityPrizeInfo = activityService.getActivityPrizeInfoList(code);
        if (activityPrizeInfo == null) return RGenerator.resCustom(-1, "没有活动信息");
        return RGenerator.resSuccess(activityPrizeInfo);
    }

    @GetMapping("getJoinUserList")
    public ResEntity<List<User>> getJoinUserList(@RequestParam @NotBlank(message = "code不能为空") String code) {
        return RGenerator.resSuccess(activityService.getJoinUserList(code));
    }

    @GetMapping("winPrize")
    public ResEntity<List<ActivityWinUser>> winPrize(@RequestParam
                                                         @NotBlank(message = "code不能为空") String code,
                                                     @NotNull(message = "奖品不能为空") Integer prizeId) {
        return RGenerator.resSuccess(activityService.winUser(code,prizeId));
    }



}
