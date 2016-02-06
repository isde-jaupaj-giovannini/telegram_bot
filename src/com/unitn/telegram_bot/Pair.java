package com.unitn.telegram_bot;

import lombok.Data;

/**
 * Created by erinda on 2/6/16.
 */
@Data
public class Pair<T, T1> {
    final T key;
    final T1 value;
}
