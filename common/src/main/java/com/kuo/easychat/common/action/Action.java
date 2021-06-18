package com.kuo.easychat.common.action;

import lombok.Data;
import lombok.ToString;

/**
 * Created on 2021/6/18.
 *
 * @author Fagan Wang
 */
@Data
@ToString
public class Action {

    private String actionType;

    private String action;

    private String requestId;

    private String payload;
}
