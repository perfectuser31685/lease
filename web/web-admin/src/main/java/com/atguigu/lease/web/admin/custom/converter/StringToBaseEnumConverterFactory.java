package com.atguigu.lease.web.admin.custom.converter;

import com.atguigu.lease.model.enums.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

@Component
public class StringToBaseEnumConverterFactory implements ConverterFactory<String, BaseEnum> {
    @Override
    public <T extends BaseEnum> Converter<String, T> getConverter(Class<T> targetType) {
        //因为是在String转成目标类的时候调用的方法，所以调用工厂方法目标类是已知的
        return new Converter<String, T>() {
            @Override
            public T convert(String source) {
                for(T enumConstant : targetType.getEnumConstants()){
                    if (enumConstant.getCode().equals(Integer.valueOf(source))) {
                        return enumConstant;
                    }
                }
                throw new IllegalArgumentException("code:" + source + "非法");
            }
        };
    }
}
