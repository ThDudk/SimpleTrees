package io.github.thdudk.simpletrees;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/// Simple serializer that serializes Trees as their {@link Tree#root()} property.
///
/// This turns:
/// ```
/// {
///   "root": { // this bit will be removed
///     "data" : 4,
///     "children" : [ {
///       "data" : 2,
///       "children" : [ {
///         "data" : 1,
///         "children" : [ ]
///       }, {
///         "data" : 3,
///         "children" : [ ]
///       } ]
///     }, {
///       "data" : 6,
///       "children" : [ {
///         "data" : 5,
///         "children" : [ ]
///       }, {
///         "data" : 7,
///         "children" : [ ]
///       } ]
///     } ]
///   }
/// }
/// ```
/// Into:
/// ```
/// {
///   "data" : 4,
///   "children" : [ {
///     "data" : 2,
///     "children" : [ {
///       "data" : 1,
///       "children" : [ ]
///     }, {
///       "data" : 3,
///       "children" : [ ]
///     } ]
///   }, {
///     "data" : 6,
///     "children" : [ {
///       "data" : 5,
///       "children" : [ ]
///     }, {
///       "data" : 7,
///       "children" : [ ]
///     } ]
///   } ]
/// }
/// ```
public class TreeSerializer extends StdSerializer<Tree<Object>> {
    protected TreeSerializer() {
        super(Tree.class);
    }

    @Override
    public void serialize(Tree<Object> value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        gen.writePOJO(value.root());
    }
}
