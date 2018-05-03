package com.ctl.springmongoquerydsl.jackson;

/**
 * Created by Cesar on 08/05/2017.
 */

import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;

/**
 * This class represents all queryDSL operators and the associated Json operator.
 * <p>
 *	It permits to link the json operator to the queryDSL operator.
 */
public enum QueryDslOperator {
    EQ("$eq", Ops.EQ),
    EQ_IC("$eq_ic", Ops.EQ_IGNORE_CASE),
    NE("$ne", Ops.NE),
    LT("$lt", Ops.LT),
    GT("$gt", Ops.GT),
    LOE("$lte", Ops.LOE),
    GOE("$gte", Ops.GOE),
    LIKE("$like", Ops.LIKE),
    STARTS_WITH("$start", Ops.STARTS_WITH),
    ENDS_WITH("$end", Ops.ENDS_WITH),
    AND("$and", Ops.AND),
    OR("$or", Ops.OR),
    BETWEEN("$between", Ops.BETWEEN),
    IN("$in", Ops.IN),
    NOT_IN("$nin", Ops.NOT_IN),
    STRING_CONTAINS("$contains", Ops.STRING_CONTAINS),
    STRING_CONTAINS_IC("$containsIc", Ops.STRING_CONTAINS_IC);

    /**
     * the associated json operator
     */
    private String json;
    /**
     * the associated queryDSL operator
     */
    private Operator DSLOperator;

    /**
     * Default constructor of {@link QueryDslOperator}
     *
     * @param json the associated json operator
     * @param DSLOperator the associated queryDSL operator
     */
    QueryDslOperator(String json, Operator DSLOperator) {
        this.json = json;
        this.DSLOperator = DSLOperator;
    }

    /**
     * Get the expression operator of the specified queryDSL operator
     *
     * @param op the queryDSL operator
     * @return the expression operator
     */
    public static QueryDslOperator getFromDsl(Operator op) {
        for (QueryDslOperator queryDslOperator : QueryDslOperator.values()) {
            if (queryDslOperator.getDSLOperator().equals(op)) {
                return queryDslOperator;
            }
        }
        throw new IllegalArgumentException("Illegal operator " + op);
    }

    /**
     * get the expression operator from the json
     *
     * @param jsonValue the json value of the operator
     * @return the expression operator
     */
    public static QueryDslOperator getFromJson(String jsonValue) {
        for (QueryDslOperator expressionOperator : QueryDslOperator.values()) {
            if (expressionOperator.getJson().equals(jsonValue)) {
                return expressionOperator;
            }
        }
        throw new IllegalArgumentException("QueryDSL operator " + jsonValue + " unknown.");
    }

    public String getJson() {
        return json;
    }

    public Operator getDSLOperator() {
        return DSLOperator;
    }

    @Override
    public String toString() {
        return json;
    }
}
