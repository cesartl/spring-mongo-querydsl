package com.ctl.springmongoquerydsl.jackson;

import com.ctl.springmongoquerydsl.utils.MyStreamUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.collect.Lists;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.PathBuilderValidator;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static com.querydsl.core.types.dsl.Expressions.constant;


/**
 * Permits to deserialize a json predicate and orderBys into queryDSL object.
 */
public class DefaultQueryDslDeserializer<T> extends StdDeserializer<Predicate> {

    /**
     * the jackson object mapper
     */
    protected final ObjectMapper mapper;
    /**
     * The value converter
     */
    protected final JsonTypeConverter converter;

    protected final Class<? extends Predicate> entityClass;

    /**
     * Default constructor of {@link DefaultQueryDslDeserializer}
     */
    public DefaultQueryDslDeserializer(Class<? extends Predicate> entityClass) {
        super(Predicate.class);
        this.mapper = new ObjectMapper();
        this.converter = new JsonTypeConverter();
        this.entityClass = entityClass;
    }

    @Override
    public Predicate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        final JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        return buildPredicates(jsonNode, this.entityClass);
    }

    /**
     * Create a queryDSL predicate from the specified json
     *
     * @param json        the json predicate
     * @param entityClass the root entity class
     * @return the queryDSL predicate
     */
    public BooleanExpression deserializePredicate(String json, Class<?> entityClass) {
        try {
            JsonNode jsonNode = mapper.readTree(json);
            return buildPredicates(jsonNode, entityClass);
        } catch (Exception e) {
            throw new DSLJsonDeserializerException("Unable to deserialize json in queryDSL predicate", e);
        }
    }

    /**
     * Create a collection of queryDSL order bys from the specified json
     *
     * @param json        the json order bys
     * @param entityClass the root entity class
     * @return the collection of queryDSL order bys
     */
    public List<OrderSpecifier> deserializeOrderBys(String json, Class<?> entityClass) {
        try {
            JsonNode jsonNode = mapper.readTree(json);
            return buildOrderBys(jsonNode, entityClass);
        } catch (Exception e) {
            throw new DSLJsonDeserializerException("Unable to deserialize json in queryDSL order bys", e);
        }
    }

    /**
     * Build all order specifier from the specified JSONObject
     */
    private List<OrderSpecifier> buildOrderBys(JsonNode rootNode, Class<?> entityClass) {

        return MyStreamUtils.fromIterator(rootNode.fields())
                .map(node -> buildPathAndOrderBy(node.getKey(), node.getValue(), entityClass))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Build an order by expression
     */
    protected List<OrderSpecifier> buildPathAndOrderBy(String key, JsonNode value, Class<?> entityClass) {
        EntityPath entityPath = buildPath(key, entityClass);
        return buildOrderBy(entityPath, value);
    }

    @SuppressWarnings("unchecked")
    protected List<OrderSpecifier> buildOrderBy(EntityPath entityPath, JsonNode value) {
        if (!value.isObject() && value.isTextual()) {
            return Lists.newArrayList(new OrderSpecifier(convert(value.asText(), Order.class), entityPath));
        }
        throw new IllegalArgumentException("The object value have to be a string : " + value.toString());
    }

    /**
     * Build all predicates from the specified map
     */
    protected BooleanExpression buildPredicates(JsonNode rootNode, Class<?> entityClass) {
        List<BooleanExpression> expressions = MyStreamUtils.fromIterator(rootNode.fields())
                .map(node -> buildPredicate(node.getKey(), node.getValue(), entityClass))
                .collect(Collectors.toList());
        return Expressions.allOf(expressions.toArray(new BooleanExpression[expressions.size()]));
    }

    /**
     * Build a predicate with the specified key and object value
     */
    protected BooleanExpression buildPredicate(String key, JsonNode nodeValue, Class<?> entityClass) {
        Optional<QueryDslOperator> operator = getOperator(key);
        if (operator.isPresent()) {
            // the key is an operator
            // it is an array of and or or predicates
            return buildOrAndPredicate(nodeValue, operator.get(), entityClass);
        } else {
            // build the path
            EntityPath entityPath = buildPath(key, entityClass);
            return buildBasePredicate(entityPath, nodeValue);
        }
    }

    /**
     * Build base predicate  : path - operator - value
     */
    protected BooleanExpression buildBasePredicate(EntityPath entityPath, JsonNode nodeValue) {
        List<Expression> expressions = new ArrayList<>();
        // add the path to the expressions list
        expressions.add(entityPath);
        QueryDslOperator valueOp = QueryDslOperator.EQ;
        // if it is an object like { "$lt" : "value" }
        if (nodeValue.isObject()) {
            Iterator<Entry<String, JsonNode>> fields = nodeValue.fields();
            while (fields.hasNext()) {
                Entry<String, JsonNode> objectNode = fields.next();
                // the key is the operator
                valueOp = QueryDslOperator.getFromJson(objectNode.getKey());
                if (valueOp.equals(QueryDslOperator.BETWEEN)) {
                    MyStreamUtils.fromIterator(objectNode.getValue().elements())
                            .forEach(v -> expressions.add(createValue(v, entityPath)));
                } else if (valueOp.equals(QueryDslOperator.IN) || valueOp.equals(QueryDslOperator.NOT_IN)) {
                    expressions.add(constant(
                            MyStreamUtils.fromIterator(objectNode.getValue().elements())
                                    .map(v -> (Object) convert(v.asText(), entityPath.getType()))
                                    .collect(Collectors.toList())));
                } else {
                    // simple value
                    expressions.add(createValue(objectNode.getValue(), entityPath));
                }
            }
        } else {
            // it is a value
            expressions.add(createValue(nodeValue, entityPath));
        }
        return Expressions.predicate(valueOp.getDSLOperator(), expressions.toArray(new Expression[expressions.size()]));
    }

    /**
     * create a queryDSL constant value for the given json node
     */
    protected Expression createValue(JsonNode value, EntityPath entityPath) {
        return constant(convert(value.asText(), entityPath.getType()));
    }

    /**
     * Build a predicate with operator and, or (array of predicates)
     */
    protected BooleanExpression buildOrAndPredicate(JsonNode nodeValue, QueryDslOperator operator, Class<?> entityClass) {
        List<BooleanExpression> booleanExpressions = new ArrayList<>();
        // the key is an operator ($and - $or - ...)
        if (nodeValue.isArray()) {
            Iterator<JsonNode> elements = nodeValue.elements();
            while (elements.hasNext()) {
                JsonNode element = elements.next();
                if (element.isObject()) {
                    booleanExpressions.add(buildPredicates(element, entityClass));
                } else {
                    throw new IllegalStateException("expected to have an object instead of " + element.toString());
                }
            }
            if (operator.equals(QueryDslOperator.AND)) {
                return Expressions.allOf(booleanExpressions.toArray(new BooleanExpression[booleanExpressions.size()]));
            } else if (operator.equals(QueryDslOperator.OR)) {
                return Expressions.anyOf(booleanExpressions.toArray(new BooleanExpression[booleanExpressions.size()]));
            } else {
                throw new IllegalStateException("Don't supported operator here : " + operator);
            }
        } else {
            throw new IllegalStateException("expected to have an array type instead of "
                    + nodeValue.toString());
        }
    }

    /**
     * Cast the json string value into an object of the associated field type
     *
     * @param value the string value
     * @param type  the field type
     * @return the value transformed into the associated field type
     */
    protected <T> T convert(String value, Class<T> type) {
        return converter.convert(value, type);
    }

    /**
     * Build a queryDSL path from the specified string path
     *
     * @param path        the string path. ex : content.product.packaging
     * @param entityClass the entity root class
     * @return the queryDSL path
     */
    @SuppressWarnings("unchecked")
    protected EntityPath<?> buildPath(String path, Class entityClass) {
        String[] paths = path.split("\\.");
        if (paths.length > 0) {
            PathBuilder builder = new PathBuilder(entityClass, paths[0], PathBuilderValidator.FIELDS);
            for (int i = 1; i < paths.length; i++) {
                builder = builder.get(paths[i]);
            }
            return builder;
        } else {
            throw new IllegalArgumentException("The specified path is incorrect : " + path);
        }
    }

    /**
     * Get the JSONOperator from the json value
     *
     * @param operator the json operator value
     * @return optional operator
     */
    protected Optional<QueryDslOperator> getOperator(String operator) {
        try {
            return Optional.of(QueryDslOperator.getFromJson(operator));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * QueryDslToJsonDeserializer exception
     */
    public static class DSLJsonDeserializerException extends RuntimeException {

        public DSLJsonDeserializerException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}