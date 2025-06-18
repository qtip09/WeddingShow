package vip.maosi.weddingServer.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.tomcat.util.buf.UEncoder;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import vip.maosi.weddingServer.config.WeddingShowConfig;
import vip.maosi.weddingServer.controller.bo.addActivityPrizeBo;
import vip.maosi.weddingServer.controller.bo.addActivityWinSpecifyBo;
import vip.maosi.weddingServer.controller.bo.editActivityPrizeBo;
import vip.maosi.weddingServer.domain.*;
import vip.maosi.weddingServer.dto.*;
import vip.maosi.weddingServer.mapper.ActivityMapper;
import vip.maosi.weddingServer.service.wx.WXService;
import vip.maosi.weddingServer.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@EnableConfigurationProperties(WeddingShowConfig.class)
public class ActivityService extends ServiceImpl<ActivityMapper, Activity> {
    @Autowired
    ActivityPrizeService activityPrizeService;
    @Autowired
    ActivityJoinService activityJoinService;
    @Autowired
    ActivityWinService activityWinService;
    @Autowired
    WXService wxService;
    @Autowired
    UserService userService;
    @Autowired
    ActivityPrizeFileRelService activityPrizeFileRelService;
    @Autowired
    FileService fileService;
    @Autowired
    activityWinSpecifyService activityWinSpecifyService;

    private final WeddingShowConfig weddingShowConfig;

    public ActivityService(WeddingShowConfig weddingShowConfig){
        this.weddingShowConfig = weddingShowConfig;
    }

    private static final Object obj = new Object();

    public ActivityInfoDto getActivityInfo(String code) {
        final Activity activity = getActivity(code);
        if (activity == null) return null;
        val activityInfoDto = new ActivityInfoDto();
        BeanUtils.copyProperties(activity, activityInfoDto);
        val activityPrize = activityPrizeService.list(Wrappers
                .<ActivityPrize>lambdaQuery()
                .eq(ActivityPrize::getActivityId, activity.getId())
                        .eq(ActivityPrize::getDeleted,false)
                .orderByAsc(ActivityPrize::getOrder));
        activityInfoDto.setPrizeList(activityPrize);
        return activityInfoDto;
    }

    public ActivityPrizeInfoDto getActivityPrizeInfoList(String code){
        final Activity activity = getActivity(code);
        if (activity == null) return null;
        val activityInfoDto = new ActivityInfoDto();
        BeanUtils.copyProperties(activity, activityInfoDto);
        val activityPrize = activityPrizeService.list(Wrappers
                .<ActivityPrize>lambdaQuery()
                .eq(ActivityPrize::getActivityId, activity.getId())
                        .eq(ActivityPrize::getDeleted,false)
                .orderByAsc(ActivityPrize::getOrder));
        List<Integer> activityPrizeId = activityPrize.stream().map(ActivityPrize::getId).distinct().collect(Collectors.toList());
        LambdaQueryWrapper<ActivityPrizeFileRel> lqwRel = new LambdaQueryWrapper<>();
        lqwRel.in(ActivityPrizeFileRel::getActivityPrizeId,activityPrizeId);
        lqwRel.eq(ActivityPrizeFileRel::getDeleted,false);
        val activityPrizeRel = activityPrizeFileRelService.list(lqwRel);
        List<Integer> fileId = activityPrizeRel.stream().map(ActivityPrizeFileRel::getFileId).distinct().collect(Collectors.toList());
        LambdaQueryWrapper<File> lqwFile = new LambdaQueryWrapper<>();
        lqwFile.in(File::getId,fileId);
        lqwFile.eq(File::getDeleted,false);
        List<File> file = new ArrayList<>();
        file = fileService.list(lqwFile);
        List<File> finalFile = file;
        val activityPrizeDto = activityPrize.stream().map(prizeTemp->{
                    ActivityPrizeDto dto = new ActivityPrizeDto();
                    dto.setActivityId(prizeTemp.getActivityId());
                    dto.setId(prizeTemp.getId());
                    dto.setPrizeName(prizeTemp.getPrizeName());
                    dto.setCount(prizeTemp.getCount());
                    dto.setOrder(prizeTemp.getOrder());
                    val rel = activityPrizeRel.stream().filter(relTemp->relTemp.getActivityPrizeId().equals(prizeTemp.getId())).collect(Collectors.toList());
                    if (rel.size()==1){
                        dto.setRelId(rel.get(0).getId());
                        dto.setFileId(rel.get(0).getFileId());
                        val f = finalFile.stream().filter(fTemp->fTemp.getId().equals(dto.getFileId())).collect(Collectors.toList());
                        if (f.size()==1){
                            File ff = f.get(0);
                            dto.setFileName(ff.getFileName());
                            String path = weddingShowConfig.getLocal().getAddress()+"/"+ff.getId();
                            dto.setFilePath(path);
                            dto.setFileType(ff.getType());
                        }
                    }
                    return dto;
                }).collect(Collectors.toList());
        val activityPrizeInfoDto = new ActivityPrizeInfoDto();
        activityPrizeInfoDto.setActivityPrizeDtoList(activityPrizeDto);
        return activityPrizeInfoDto;
    }

    @Transactional
    public Boolean addActicityPrize(addActivityPrizeBo bo){
        ActivityPrize activityPrize = new ActivityPrize();
        activityPrize.setPrizeName(bo.getPrizeName());
        activityPrize.setCount(bo.getCount());
        activityPrize.setActivityId(1);
        boolean flag = activityPrizeService.save(activityPrize);
        if (!flag){
            throw new RuntimeException("新增奖品异常");
        }
        ActivityPrizeFileRel rel = new ActivityPrizeFileRel();
        rel.setFileId(bo.getFileId());
        rel.setActivityPrizeId(activityPrize.getId());
        rel.setDeleted(false);
        rel.setCreateDate(new Date());
        flag = activityPrizeFileRelService.save(rel);
        if (!flag){
            throw new RuntimeException("新增奖品与文件关联关系异常");
        }
        return flag;

    }
    @Transactional
    public Boolean delActivityPrize(Integer activityPrizeId){
        ActivityPrize activityPrize = activityPrizeService.getById(activityPrizeId);
        activityPrize.setDeleted(true);
        Boolean flag = activityPrizeService.updateById(activityPrize);
        if (!flag){
            throw new RuntimeException("失效奖品异常");
        }
        return flag;
    }

    @Transactional
    public Boolean editActicityPrize(editActivityPrizeBo bo){
        boolean flag = true;
        ActivityPrize activityPrize = activityPrizeService.getById(bo.getPrizeId());
        if (activityPrize.getDeleted()){
            throw new RuntimeException("奖品已失效");
        }
        LambdaQueryWrapper<ActivityPrizeFileRel> lqw = new LambdaQueryWrapper();
        lqw.eq(ActivityPrizeFileRel::getActivityPrizeId,bo.getPrizeId());
        lqw.eq(ActivityPrizeFileRel::getDeleted,false);
        ActivityPrizeFileRel rel = activityPrizeFileRelService.getOne(lqw);
        if (rel != null && !rel.getFileId().equals(bo.getFileId())){
            LambdaUpdateWrapper<ActivityPrizeFileRel> luw = new LambdaUpdateWrapper<>();
            luw.eq(ActivityPrizeFileRel::getActivityPrizeId,bo.getPrizeId());
            luw.eq(ActivityPrizeFileRel::getDeleted,false);
            luw.set(ActivityPrizeFileRel::getDeleted,true);
            flag = activityPrizeFileRelService.update(luw);
            if (!flag){
                throw new RuntimeException("失效奖品与文件关联关系异常");
            }
            ActivityPrizeFileRel relNew = new ActivityPrizeFileRel();
            relNew.setFileId(bo.getFileId());
            relNew.setActivityPrizeId(activityPrize.getId());
            relNew.setDeleted(false);
            relNew.setCreateDate(new Date());
            flag = activityPrizeFileRelService.save(relNew);
            if (!flag){
                throw new RuntimeException("新增奖品与文件关联关系异常");
            }
        }
        activityPrize.setCount(bo.getCount());
        activityPrize.setPrizeName(bo.getPrizeName());
        flag = activityPrizeService.updateById(activityPrize);
        if (!flag){
            throw new RuntimeException("更新奖品异常");
        }
        return flag;
    }





    public List<ActivityWinUser> getActivityWinList(String code) {
        final Activity activity = getActivity(code);
        if (activity == null) return null;
        val activityWinList = activityWinService.list(Wrappers
                .<ActivityWin>lambdaQuery()
                .eq(ActivityWin::getActivityId, activity.getId()));
        val winLists = new ArrayList<ActivityWinUser>();
        for (ActivityWin win : activityWinList) {
            // 查奖品
            val activityPrize = activityPrizeService.getById(win.getActivityPrizeId());
            // 查用户
            val user = userService.getById(win.getUid());
            if (user == null) continue;
            val activityWinUser = new ActivityWinUser()
                    .setUserName(user.getNickName())
                    .setOpenid(user.getOpenid())
                    .setAvatarUrl(user.getAvatarUrl())
                    .setPrizeName(activityPrize.getPrizeName())
                    .setPrizeNum(1)
                    .setIsGet(win.getIsGet() != null && win.getIsGet());
            winLists.add(activityWinUser);
        }
        return winLists;
    }

    public List<ActivityWinSpecifyDto> getActivityWinSpecifyList(ActivityPrize activityPrize){
        List<ActivityWinSpecifyDto> activityWinSpecifyDtoList = new ArrayList<>();
        LambdaQueryWrapper<ActivityWinSpecify> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ActivityWinSpecify::getActivityPrizeId,activityPrize.getId());
        lqw.eq(ActivityWinSpecify::getActivityId,1);
        lqw.eq(ActivityWinSpecify::getDeleted,false);
        List<ActivityWinSpecify> activityWinSpecifyList = activityWinSpecifyService.list(lqw);
        if (activityWinSpecifyList.size() == 0){
            return activityWinSpecifyDtoList;
        }
        List<Integer> uidList = activityWinSpecifyList.stream().distinct().map(ActivityWinSpecify::getUid).collect(Collectors.toList());
        LambdaQueryWrapper<User> lqwUser = new LambdaQueryWrapper<>();
        lqwUser.in(User::getId,uidList);
        List<User> userList = userService.list(lqwUser);
        activityWinSpecifyDtoList = activityWinSpecifyList.stream().map(temp -> {
            ActivityWinSpecifyDto dto = new ActivityWinSpecifyDto();
            dto.setId(temp.getId());
            dto.setActivityId(temp.getActivityId());
            dto.setActivityPrizeId(temp.getActivityPrizeId());
            dto.setUid(temp.getUid());
            User user = userList.stream().filter(p -> p.getId().equals(temp.getUid())).collect(Collectors.toList()).get(0);
            dto.setOpenId(user.getOpenid());
            dto.setNickName(user.getNickName());
            dto.setCreateDate(temp.getCreateDate());
            dto.setUpdateDate(temp.getUpdateDate());
            dto.setDeleted(temp.getDeleted());
            return dto;
        }).collect(Collectors.toList());
        return activityWinSpecifyDtoList;



    }
    public Boolean addActivityWinSpecify(addActivityWinSpecifyBo bo){
        User user = userService.getById(bo.getUid());
        if (!user.getNickName().equals(bo.getName())){
            throw new RuntimeException("用户不匹配");
        }

        ActivityPrize activityPrize = activityPrizeService.getById(bo.getPrizeId());
        if (activityPrize == null || activityPrize.getDeleted()){
            throw new RuntimeException("奖品状态异常");
        }
        LambdaQueryWrapper<ActivityWinSpecify> lqw = new LambdaQueryWrapper();
        lqw.eq(ActivityWinSpecify::getActivityPrizeId,bo.getPrizeId());
        lqw.eq(ActivityWinSpecify::getDeleted,false);
        List<ActivityWinSpecify> activityWinSpecifyList = activityWinSpecifyService.list(lqw);
        if (activityWinSpecifyList.size() + 1 > activityPrize.getCount()){
            throw new RuntimeException("奖品可分配数量不足");
        }
        List<ActivityWinSpecify> activityWinSpecifyFilterList = activityWinSpecifyList.stream().filter(temp -> temp.getUid().equals(bo.getUid())).collect(Collectors.toList());
        if (activityWinSpecifyFilterList.size() >0){
            throw new RuntimeException("该奖品已分配给该用户");
        }
        ActivityWinSpecify activityWinSpecify = new ActivityWinSpecify();
        activityWinSpecify.setActivityId(1);
        activityWinSpecify.setActivityPrizeId(bo.getPrizeId());
        activityWinSpecify.setUid(bo.getUid());
        activityWinSpecify.setCreateDate(new Date());
        activityWinSpecify.setDeleted(false);
        Boolean flag = activityWinSpecifyService.save(activityWinSpecify);
        return flag;
    }

    public Boolean delActivityWinSpecify(Integer activityWinSpecifyId){
        Boolean flag = activityWinSpecifyService.removeById(activityWinSpecifyId);
        return flag;
    }


    public Page<User> searchUsers(String name,Integer pageNum,Integer pageSize){
        // 创建分页对象
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotBlank(name),User::getNickName,name);
        return userService.page(page,lqw);
    }
    public Pair<Integer, String> joinActivity(String openid, String code) {
        val activity = getActivity(code);
        val user = wxService.getUser(openid);
        if (user == null) return Pair.of(-1, "用户不存在");
        if (activity == null) return Pair.of(-1, "活动不存在");
        val join = activityJoinService.getOne(Wrappers.<ActivityJoin>lambdaQuery()
                .eq(ActivityJoin::getUid, user.getId())
                .eq(ActivityJoin::getActivityId, activity.getId()));
        if (activity.getActivityEndDate().before(new Date())) return Pair.of(-1, "活动已截止");
        if (join != null) {
            return Pair.of(-1, "已加入活动");
        }
        val save = activityJoinService.save(new ActivityJoin()
                .setActivityId(activity.getId())
                .setUid(user.getId())
                .setDate(new Date()));
        if (save) return Pair.of(0, "加入成功");
        return Pair.of(-2, "加入失败");
    }

    public Triple<Integer, ActivityPrize, String> getActivityStatus(String openid, String code) {
        val activity = getActivity(code);
        val user = wxService.getUser(openid);
        if (user == null) return Triple.of(-1, null, "用户不存在");
        if (activity == null) return Triple.of(-1, null, "活动不存在");
        val exists = activityJoinService.getBaseMapper().exists(Wrappers.<ActivityJoin>lambdaQuery()
                .eq(ActivityJoin::getActivityId, activity.getId())
                .eq(ActivityJoin::getUid, user.getId()));
        if (exists && activity.getActivityEndDate().after(new Date())) {
            return Triple.of(1, null, "已加入活动，活动没开奖");
        } else if (exists && activity.getActivityEndDate().before(new Date())) {
            // 判断是否开奖
            val existsWin = activityWinService.getBaseMapper().exists(Wrappers.<ActivityWin>lambdaQuery()
                    .eq(ActivityWin::getActivityId, activity.getId()));
            /*if (existsWin) {
                // 判断是否中奖
                return isUserPrize(activity, user);
            } else {
                synchronized (obj) {
                    // 未开奖，开奖操作并返回中奖状态
                    val activityPrizes = activityPrizeService.list(Wrappers.<ActivityPrize>lambdaQuery().eq(ActivityPrize::getActivityId, activity.getId()));
                    val activityJoins = activityJoinService.list(Wrappers.<ActivityJoin>lambdaQuery().eq(ActivityJoin::getActivityId, activity.getId()));
                    for (ActivityPrize activityPrize : activityPrizes) {
                        for (int i = 0; i < activityPrize.getCount(); i++) {
                            ActivityJoin join = getRandomValue(activityJoins);
                            if (join == null) break;
                            val activityWin = new ActivityWin();
                            activityWin.setActivityId(activity.getId())
                                    .setUid(join.getUid())
                                    .setActivityPrizeId(activityPrize.getId())
                                    .setDate(new Date());
                            activityWinService.save(activityWin);
                        }
                    }
                }
                // 判断是否中奖
                return isUserPrize(activity, user);
            }*/
            /*
            * 不需要小程序开奖
            * */
            if (existsWin){
                return isUserPrize(activity, user);
            }else {
                return Triple.of(1, null, "已加入活动，活动没开奖");
            }
        }
        return Triple.of(0, null, "未加入当前活动");
    }

    public static <T> T getRandomValue(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(list.size());
        val item = list.get(randomIndex);
        list.remove(item);
        return item;
    }

    @NotNull
    private Triple<Integer, ActivityPrize, String> isUserPrize(Activity activity, User user) {
        // 已开奖，查询win表，返回中奖状态
        val winUser = activityWinService.getOne(Wrappers.<ActivityWin>lambdaQuery()
                .eq(ActivityWin::getUid, user.getId())
                .eq(ActivityWin::getActivityId, activity.getId()));
        if (winUser != null) {
            // 中奖
            val activityPrize = activityPrizeService.getById(winUser.getActivityPrizeId());
            return Triple.of(2, activityPrize, "恭喜你，已中奖！");
        } else {
            // 未中奖
            return Triple.of(3, null, "很遗憾，未中奖！");
        }
    }

    private Activity getActivity(String code) {
        return getOne(Wrappers.<Activity>lambdaQuery().eq(Activity::getCode, code));
    }



    public List<User> getJoinUserList(String code){
        Activity activity = getActivity(code);
        if (activity == null || activity.getActivityEndDate().before(new Date())){
            throw new RuntimeException("活动异常或已开奖");
        }
        LambdaQueryWrapper<ActivityJoin> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ActivityJoin::getActivityId,activity.getId());
        List<ActivityJoin> activityJoinList = activityJoinService.list(lqw);
        List<Integer> uidList = activityJoinList.stream().map(ActivityJoin::getUid).collect(Collectors.toList());
        if (uidList.size() <=0){
            throw new RuntimeException("活动参与人数异常");
        }
        LambdaQueryWrapper<User> lqwUser = new LambdaQueryWrapper<>();
        lqwUser.notLike(User::getNickName,"微信用户");
        lqwUser.ne(User::getNickName,"");
        List<User> userList = userService.list(lqwUser);
        userList = userList.stream().filter(user -> uidList.contains(user.getId())).collect(Collectors.toList());
        if (userList.size() <=0){
            throw new RuntimeException("活动参与人数异常");
        }
        return userList;
    }

    public List<ActivityWinUser> winUser(String code,Integer prizeId){
        Activity activity = getActivity(code);
        if (activity == null || activity.getActivityEndDate().before(new Date())){
            throw new RuntimeException("活动异常或已开奖");
        }
        ActivityPrize activityPrize = activityPrizeService.getById(prizeId);
        if (activityPrize == null || activityPrize.getDeleted().equals(true) || activityPrize.getCount()<=0){
            throw new RuntimeException("奖品异常");
        }
        LambdaQueryWrapper<User> lqwUser = new LambdaQueryWrapper<>();
        lqwUser.notLike(User::getNickName,"微信用户");
        lqwUser.ne(User::getNickName,"");
        List<User> userList = userService.list(lqwUser);
        List<ActivityJoin> activityJoins = activityJoinService.list(Wrappers.<ActivityJoin>lambdaQuery().eq(ActivityJoin::getActivityId, activity.getId()));
        List<Integer> activityJoinUids = activityJoins.stream().map(ActivityJoin::getUid).collect(Collectors.toList());
        List<User> joinUserList = userList.stream().filter(user -> activityJoinUids.contains(user.getId())).collect(Collectors.toList());

        //List<Integer> joinUidList = activityJoins.stream().map(ActivityJoin::getUid).collect(Collectors.toList());
        List<Integer> joinUidList = joinUserList.stream().map(User::getId).collect(Collectors.toList());
        activityJoins = activityJoins.stream().filter(activityJoin -> joinUidList.contains(activityJoin.getUid())).collect(Collectors.toList());
        List<ActivityWinSpecify> activityWinSpecifyList = activityWinSpecifyService.list(Wrappers.<ActivityWinSpecify>lambdaQuery()
                .eq(ActivityWinSpecify::getActivityPrizeId,prizeId)
                .eq(ActivityWinSpecify::getActivityId,activity.getId())
                .eq(ActivityWinSpecify::getDeleted,false));

        List<ActivityWinUser> activityWinUserList = new ArrayList<>();
        for (int i=0;i<activityPrize.getCount();i++){
            val activityWin = new ActivityWin();
            val activityWinUser = new ActivityWinUser();
            for (int j=0;j<activityWinSpecifyList.size();j++){
                ActivityWinSpecify activityWinSpecify = activityWinSpecifyList.get(j);
                if (joinUidList.contains(activityWinSpecify.getUid())){
                    activityWin.setActivityId(activity.getId())
                            .setUid(activityWinSpecify.getUid())
                            .setActivityPrizeId(activityPrize.getId())
                            .setDate(new Date());
                    User winUser = joinUserList.stream().filter(user -> user.getId().equals(activityWin.getUid())).findFirst().get();
                    activityWinUser.setUserName(winUser.getNickName())
                            .setAvatarUrl(winUser.getAvatarUrl())
                                    .setOpenid(winUser.getOpenid())
                                            .setPrizeName(activityPrize.getPrizeName())
                                                    .setPrizeNum(1)
                                                            .setIsGet(false);
                    activityWinService.save(activityWin);
                    activityWinSpecifyList.remove(j);
                    joinUserList = joinUserList.stream().filter(joinuser->!joinuser.getId().equals(activityWin.getUid())).collect(Collectors.toList());
                    activityJoins = activityJoins.stream().filter(activityJoin -> !activityJoin.getUid().equals(activityWin.getUid())).collect(Collectors.toList());
                    activityWinUserList.add(activityWinUser);
                    break;
                }
            }
            if (joinUserList.size()<=0 || activityJoins.size()<=0){
                break;
            }

            if (activityWin.getId() == null || activityWin.getId().equals(0)){
                ActivityJoin join = getRandomValue(activityJoins);
                if (join == null) break;
                activityWin.setActivityId(activity.getId())
                        .setUid(join.getUid())
                        .setActivityPrizeId(activityPrize.getId())
                        .setDate(new Date());
                log.error(JSONObject.valueToString(activityWin));
                log.error(JSONObject.valueToString(joinUserList));
                User winUser = joinUserList.stream().filter(user -> user.getId().equals(activityWin.getUid())).findFirst().get();
                activityWinUser.setUserName(winUser.getNickName())
                        .setAvatarUrl(winUser.getAvatarUrl())
                        .setOpenid(winUser.getOpenid())
                        .setPrizeName(activityPrize.getPrizeName())
                        .setPrizeNum(0)
                        .setIsGet(false);
                activityWinService.save(activityWin);
                activityWinUserList.add(activityWinUser);
            }

        }
        return activityWinUserList;

    }
}
