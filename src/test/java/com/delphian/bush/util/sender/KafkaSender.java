package com.delphian.bush.util.sender;

import java.util.Map;

public interface KafkaSender<T> {

    Map<String, String> getKeyProperties(T t);

    String getTopic();

    Class<T> getType();

}
