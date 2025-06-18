package vip.maosi.weddingServer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vip.maosi.weddingServer.controller.bo.addActivityPrizeBo;
import vip.maosi.weddingServer.controller.bo.addActivityWinSpecifyBo;
import vip.maosi.weddingServer.controller.bo.editActivityPrizeBo;
import vip.maosi.weddingServer.domain.ActivityPrize;
import vip.maosi.weddingServer.domain.User;
import vip.maosi.weddingServer.dto.ActivityInfoDto;
import vip.maosi.weddingServer.dto.ActivityPrizeInfoDto;
import vip.maosi.weddingServer.dto.ActivityWinSpecifyDto;
import vip.maosi.weddingServer.dto.ActivityWinUser;
import vip.maosi.weddingServer.response.RGenerator;
import vip.maosi.weddingServer.response.ResEntity;
import vip.maosi.weddingServer.service.ActivityJoinService;
import vip.maosi.weddingServer.service.ActivityPrizeService;
import vip.maosi.weddingServer.service.ActivityService;
import vip.maosi.weddingServer.service.ActivityWinService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/activity")
public class ActivityController {
    @Autowired
    ActivityService activityService;
    @Autowired
    ActivityPrizeService activityPrizeService;
    @Autowired
    ActivityJoinService activityJoinService;
    @Autowired
    ActivityWinService activityWinService;
    @Autowired
    BulletController bulletController;

    /**
     * 根据活动code获取活动详情和奖品列表
     *
     * @param code
     * @return
     */
    @GetMapping("/getActivityPrizes")
    public ResEntity<ActivityInfoDto> getActivityPrizes(@RequestParam @NotBlank(message = "code不能为空") String code) {
        val activityInfo = activityService.getActivityInfo(code);
        if (activityInfo == null) return RGenerator.resCustom(-1, "没有活动信息");
        return RGenerator.resSuccess(activityInfo);
    }


    @GetMapping("/getActivityPrizeList")
    public ResEntity<ActivityPrizeInfoDto> getActivityPrizeList(@RequestParam @NotBlank(message = "code不能为空") String code) {
        val activityPrizeInfo = activityService.getActivityPrizeInfoList(code);
        if (activityPrizeInfo == null) return RGenerator.resCustom(-1, "没有活动信息");
        return RGenerator.resSuccess(activityPrizeInfo);
    }

    @PostMapping("/addActivityPrize")
    public ResEntity<Boolean> addActivityPrize(@RequestBody addActivityPrizeBo bo){
        return RGenerator.resSuccess(activityService.addActicityPrize(bo));
    }

    @GetMapping("/delActivityPrize")
    public ResEntity<Boolean> addActivityPrize(@RequestParam Integer activityPrizeId){
        return RGenerator.resSuccess(activityService.delActivityPrize(activityPrizeId));
    }

    @PostMapping("/editActivityPrize")
    public ResEntity<Boolean> editActivityPrize(@RequestBody editActivityPrizeBo bo){
        return RGenerator.resSuccess(activityService.editActicityPrize(bo));
    }

    @PostMapping("getActivityWinSpecifyList")
    public ResEntity<List<ActivityWinSpecifyDto>> getActivityWinSpecifyList(@RequestBody ActivityPrize activityPrize){
        return RGenerator.resSuccess(activityService.getActivityWinSpecifyList(activityPrize));
    }
    @GetMapping("searchUsers")
    public ResEntity<Page<User>> searchUsers(@RequestParam String name, Integer pageNum, Integer pageSize){
        return RGenerator.resSuccess(activityService.searchUsers(name,pageNum,pageSize));
    }

    @PostMapping("addActivityWinSpecify")
    public ResEntity<Boolean> addActivityWinSpecify(@RequestBody addActivityWinSpecifyBo bo){
        return RGenerator.resSuccess(activityService.addActivityWinSpecify(bo));
    }

    @GetMapping("delActivityWinSpecify")
    public ResEntity<Boolean> addActivityWinSpecify(@RequestParam Integer activityWinSpecifyId){
        return RGenerator.resSuccess(activityService.delActivityWinSpecify(activityWinSpecifyId));
    }

    /**
     * 加入活动
     *
     * @param code
     * @return
     */
    @GetMapping("/joinActivity")
    public ResEntity<String> joinActivity(HttpServletRequest request,
                                          @RequestParam @NotBlank(message = "code不能为空") String code) {
        val openid = request.getHeader("openid");
        val pair = activityService.joinActivity(openid, code);
        if (pair.getLeft() == 0) {
            bulletController.sendBullet(request,"新婚快乐~");
            return RGenerator.resSuccess(pair.getRight());
        }
        return RGenerator.resCustom(pair.getLeft(), pair.getRight());
    }

    /**
     * 获取活动状态
     *
     * @param request
     * @param code    活动code
     * @return
     */
    @GetMapping("/getActivityStatus")
    public ResEntity<ActivityPrize> getActivityStatus(HttpServletRequest request,
                                                      @RequestParam @NotBlank(message = "code不能为空") String code) {
        val openid = request.getHeader("openid");
        val triple = activityService.getActivityStatus(openid, code);
        return RGenerator.resCustom(triple.getLeft(), triple.getRight(), triple.getMiddle());
    }

    /**
     * 查询中奖人列表
     * @param code
     * @return
     */
    @GetMapping("/getActivityWinList")
    public ResEntity<List<ActivityWinUser>> getActivityWinList(@RequestParam @NotBlank(message = "code不能为空") String code) {
        val list = activityService.getActivityWinList(code);
        return RGenerator.resSuccess(list);
    }


}
