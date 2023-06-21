package com.suse.oval.ovaltypes;

import com.suse.oval.TestEvaluator;

public interface BaseCriteria {

    boolean evaluate(TestEvaluator testEvaluator);
}
