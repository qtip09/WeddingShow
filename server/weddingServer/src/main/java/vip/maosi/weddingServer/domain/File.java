package vip.maosi.weddingServer.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author liudw
 * @Date 2025/4/25 16:06
 * @Version v1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName(value = "file")
public class File implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    @NotNull(message = "不能为null")
    private Integer id;

    @NotEmpty
    @NotNull(message = "不能为null")
    private String fileName;
    @NotEmpty
    @NotNull(message = "不能为null")
    private String filePath;
    private String type;
    @NotEmpty
    @NotNull(message = "不能为null")
    private Date createDate;
    @NotEmpty
    @NotNull(message = "不能为null")
    private Boolean deleted;

    private static final long serialVersionUID = 1L;

}
