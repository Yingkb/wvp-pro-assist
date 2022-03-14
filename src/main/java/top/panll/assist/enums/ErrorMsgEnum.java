package top.panll.assist.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @des:
 * @author: Yingkb
 * @create: 2022/03/10 10:45
 **/
@AllArgsConstructor
@Getter
public enum ErrorMsgEnum {
    FAILURE(101001, "操作失败"),
    INSERT_FAILURE(101002, "增加失败"),
    UPDATE_FAILURE(101003, "更改失败"),
    SELECT_FAILURE(101004, "查询失败"),
    DELETE_FAILURE(101005, "删除失败"),
    INSTANCE_NOT_EXIST(101006, "入参为空"),
    RECORD_IS_NULL(101007, "数据不存在"),
    PARAMS_MISS(101008, "参数缺失"),
    PARAMS_HAS_ERROR(101009, "数据存在错误"),
    FILE_IS_EXIST(101010,"文件为空"),

    MINIO_BUCKET_EXIST(201001,"该Bucket已存在"),
    MINIO_BUCKET_FAIL(201002,"Bucket出错"),
    ;
    private final Integer code;

    private final String msg;
}
