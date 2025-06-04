package vip.maosi.weddingServer.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import vip.maosi.weddingServer.domain.ActivityWin;
import vip.maosi.weddingServer.domain.Bullet;
import vip.maosi.weddingServer.domain.File;
import vip.maosi.weddingServer.domain.User;
import vip.maosi.weddingServer.mapper.BulletMapper;
import vip.maosi.weddingServer.mapper.FileMapper;

import java.util.Date;

@Service
public class FileService extends ServiceImpl<FileMapper, File> {

    public Integer saveFile(String fileName,String realName,String type){
        File file = new File();
        file.setFileName(realName);
        file.setFilePath(fileName);
        file.setType(type);
        file.setCreateDate(new Date());
        Boolean flag = saveOrUpdate(file);
        if (flag){
            return file.getId();
        }else {
            return 0;
        }
    }

    public File queryById(Integer id){
        LambdaQueryWrapper<File> lqw = new LambdaQueryWrapper<File>();
        lqw.eq(id != null &&  id != 0,File::getId,id);
        File file = getOne(lqw);
        return file;
    }
}
