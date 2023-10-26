package top.easydu.easymonitor.common.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * 通用枚举类
 */
public interface BaseEnum {

    default Optional<BaseEnum> getByCode(Class<BaseEnum> e, int code) {
        return Arrays.stream((BaseEnum[])e.getDeclaringClass().getEnumConstants())
                .filter(item -> item.getCode() == code)
                .findFirst();
    }

    Integer getCode();

    String getDesc();
}
