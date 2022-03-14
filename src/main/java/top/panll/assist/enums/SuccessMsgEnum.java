package top.panll.assist.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @des:
 * @author: Yingkb
 * @create: 2022/03/10 10:39
 **/
@AllArgsConstructor
@Getter
public enum SuccessMsgEnum {
    SUCCESS(200, "操作成功"),
    INSERT_SUCCESS(200, "增加成功"),
    UPDATE_SUCCESS(200, "更改成功"),
    SELECT_SUCCESS(200, "查询成功"),
    DELETE_SUCCESS(200, "删除成功")

    ;
    private final Integer code;
    private final String msg;

}
