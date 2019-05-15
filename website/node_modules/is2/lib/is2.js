/**
 * @fileOverview
 * is2 derived from is by Enrico Marino, adapted for Node.js.
 * License remains the same.
 * Slightly modified by Edmond Meinfelder
 *
 * is
 * the definitive JavaScript type testing library
 * Copyright(c) 2013 Edmond Meinfelder <edmond@stdarg.com>
 * Copyright(c) 2011 Enrico Marino <enrico.marino@email.com>
 * MIT license
 */
'use strict';
var owns = {}.hasOwnProperty;
var toString = {}.toString;
var is = exports;
is.version = '0.1.5';

var deepIs = require('deep-is');

/**
 * Test if 'value' is a type of 'type'.
 * Alias: a
 * @param value value to test.
 * @param {String} type THe name of the type.
 * @return {Boolean} true if 'value' is an arguments object, false otherwise.
 */
is.type = function (value, type) {
    return typeof value === type;
};
is.a = is.type;

/**
 * Test if 'value' is defined.
 * Alias: def
 * @param {Any} value The value to test.
 * @return {Boolean} true if 'value' is defined, false otherwise.
 */
is.defined = function (value) {
    return typeof value !== 'undefined';
};
is.def = is.defined;

/**
 * Test is 'value' is either null or undefined.
 * Alias: nullOrUndef
 * @param {Any} value value to test.
 * @return {Boolean} True if value is null or undefined, false otherwise.
 */
is.nullOrUndef = function(value) {
    return value === null || typeof value === 'undefined';
};
is.nullOrUndefined = is.nullOrUndef;

/**
 * Test if 'value' is empty. To be empty means to be an array, object or string with nothing contained.
 * @param {Any} value value to test.
 * @return {Boolean} true if 'value' is empty, false otherwise.
 */
is.empty = function (value) {
    var type = toString.call(value);

    if ('[object Array]' === type || '[object Arguments]' === type) {
        return value.length === 0;
    }

    if ('[object Object]' === type) {
        for (var key in value) if (owns.call(value, key)) return false;
        return true;
    }

    if ('[object String]' === type) {
        return value === '';
    }

    return false;
};

/**
 * Do a deep comparision of two objects for equality. Will recurse without any limits. Meant to be
 * called by equal only.
 * @param {Object} value The first object to compare.
 * @param {Object} other The second object to compare.
 * @return {Boolean} true, if the objects are equivalent, false otherwise.
 * @api private
 */
is.objEquals = function (value, other) {
    return deepIs(value, other);
};

/**
 * Test if 'value' is equal to 'other'. Works for objects and arrays and will do deep comparisions,
 * using recursion.
 * Alias: eq
 * @param {Any} value value.
 * @param {Any} other value to compare with.
 * @return {Boolean} true if 'value' is equal to 'other', false otherwise
 */
is.equal = function (value, other) {
    var type = toString.call(value);

    if (typeof value !== typeof other) {
        return false;
    }


    if (type !== toString.call(other)) {
        return false;
    }

    if ('[object Object]' === type || '[object Array]' === type) {
        return deepIs(value, other);
    } else if ('[object Function]' === type) {
        return value.prototype === other.prototype;
    } else if ('[object Date]' === type) {
        return value.getTime() === other.getTime();
    }

    return value === other;
};
is.eq = is.equal;

/**
 * JS Type definitions which cannot host values.
 * @api private
 */
var NON_HOST_TYPES = {
    'boolean': 1,
    'number': 1,
    'string': 1,
    'undefined': 1
};

/**
 * Test if 'key' in host is an object. To be hosted means host[value] is an object.
 * @param {Any} value The value to test.
 * @param {Any} host Host that may contain value.
 * @return {Boolean} true if 'value' is hosted by 'host', false otherwise.
 */
is.hosted = function (value, host) {
    if (is.nullOrUndef(value))
        return false;
    var type = typeof host[value];
    return type === 'object' ? !!host[value] : !NON_HOST_TYPES[type];
};

/**
 * Test if 'value' is an instance of 'constructor'.
 * Aliases: instOf, instanceof
 * @param {Any} value value to test.
 * @return {Boolean} true if 'value' is an instance of 'constructor'.
 */
is.instanceOf = function (value, constructor) {
    if (is.nullOrUndef(value) || is.nullOrUndef(constructor))
        return false;
    return (value instanceof constructor);
};
is.instOf = is.instanceof = is.instanceOf;

/**
 * Test if 'value' is an instance of Buffer.
 * Aliases: instOf, instanceof
 * @param {Any} value value to test.
 * @return {Boolean} true if 'value' is an instance of 'constructor'.
 */
is.buffer = function (value) {
    return Buffer.isBuffer(value);
};
is.buf = is.buffer;

/**
 * Test if 'value' is null.
 * @param {Any} value to test.
 * @return {Boolean} true if 'value' is null, false otherwise.
 */
is.null = function (value) {
    return value === null;
};

/**
 * Test if 'value' is undefined.
 * Aliases: undef, udef
 * @param {Any} value value to test.
 * @return {Boolean} true if 'value' is undefined, false otherwise.
 */
is.undefined = function (value) {
    return value === undefined;
};
is.udef = is.undef = is.undefined;

/**
 * Test if 'value' is an arguments object.
 * Alias: args
 * @param {Any} value value to test
 * @return {Boolean} true if 'value' is an arguments object, false otherwise
 */
is.arguments = function (value) {
    return '[object Arguments]' === toString.call(value);
};
is.args = is.arguments;

/**
 * Test if 'value' is an arguments object that is empty.
 * Alias: args
 * @param {Any} value value to test
 * @return {Boolean} true if 'value' is an arguments object with no args, false otherwise
 */
is.emptyArguments = function (value) {
    return '[object Arguments]' === toString.call(value) && value.length === 0;
};
is.emptyArgs = is.emptyArguments;

/**
 * Test if 'value' is an array.
 * Alias: ary, arry
 * @param {Any} value value to test.
 * @return {Boolean} true if 'value' is an array, false otherwise.
 */
is.array = function (value) {
    return '[object Array]' === toString.call(value);
};
is.ary = is.arry = is.array;

/**
 * Test if 'value' is an array containing at least 1 entry.
 * Aliases: nonEmptyArry, nonEmptyAry
 * @param {Any} value The value to test.
 * @return {Boolean} true if 'value' is an array with at least 1 value, false otherwise.
 */
is.nonEmptyArray = function (value) {
    return '[object Array]' === toString.call(value) && value.length > 0;
};
is.nonEmptyArry = is.nonEmptyAry = is.nonEmptyArray;

/**
 * Test if 'value' is an array containing no entries.
 * Aliases: emptyArry, emptyAry
 * @param {Any} value The value to test.
 * @return {Boolean} true if 'value' is an array with no elemnets.
 */
is.emptyArray = function (value) {
    return '[object Array]' === toString.call(value) && value.length === 0;
};
is.emptyArry = is.emptyAry = is.emptyArray;

/**
 * Test if 'value' is an empty array(like) object.
 * Aliases: arguents.empty, args.empty, ary.empty, arry.empty
 * @param {Any} value value to test.
 * @return {Boolean} true if 'value' is an empty array(like), false otherwise.
 */
is.array.empty = function (value) {
    return value.length === 0;
};
is.arguments.empty = is.args.empty = is.ary.empty = is.arry.empty = is.array.empty;

/**
 * Test if 'value' is an arraylike object (i.e. it has a length property with a valid value)
 * Aliases: arraylike, arryLike, aryLike
 * @param {Any} value value to test.
 * @return {Boolean} true if 'value' is an arguments object, false otherwise.
 */
is.arrayLike = function (value) {
    if (is.nullOrUndef(value))
        return false;
    return value !== undefined &&
        owns.call(value, 'length') &&
        isFinite(value.length);
};
is.arryLike = is.aryLike = is.arraylike = is.arrayLike;

/**
 * Test if 'value' is a boolean.
 * Alias: bool
 * @param {Any} value value to test.
 * @return {Boolean} true if 'value' is a boolean, false otherwise.
 */
is.boolean = function (value) {
    return '[object Boolean]' === toString.call(value);
};
is.bool = is.boolean;

/**
 * Test if 'value' is false.
 * @param {Any} value value to test.
 * @return {Boolean} true if 'value' is false, false otherwise
 */
is.false = function (value) {
    return value === false;
};

/**
 * Test if 'value' is true.
 * @param {Any} value to test.
 * @return {Boolean} true if 'value' is true, false otherwise.
 */
is.true = function (value) {
    return value === true;
};

/**
 * Test if 'value' is a date.
 * @param {Any} value value to test.
 * @return {Boolean} true if 'value' is a date, false otherwise.
 */
is.date = function (value) {
    return '[object Date]' === toString.call(value);
};

/**
 * Test if 'value' is an error object.
 * Alias: err
 * @param value value to test.
 * @return {Boolean} true if 'value' is an error object, false otherwise.
 */
is.error = function (value) {
    return '[object Error]' === toString.call(value);
};
is.err = is.error;

/**
 * Test if 'value' is a function.
 * Alias: func
 * @param {Any} value value to test.
 * @return {Boolean} true if 'value' is a function, false otherwise.
 */
is.function = function(value) {
    return '[object Function]' === toString.call(value);
};
is.func = is.function;

/**
 * Test if 'value' is a number.
 * Alias: num
 * @param {Any} value to test.
 * @return {Boolean} true if 'value' is a number, false otherwise.
 */
is.number = function (value) {
    return '[object Number]' === toString.call(value);
};
is.num = is.number;

/**
 * Test if 'value' is a positive number.
 * Alias: positiveNum, posNum
 * @param {Any} value to test.
 * @return {Boolean} true if 'value' is a number, false otherwise.
 */
is.positiveNumber = function (value) {
    return '[object Number]' === toString.call(value) && value > 0;
};
is.posNum = is.positiveNum = is.positiveNumber;

/**
 * Test if 'value' is a negative number.
 * Aliases: negNum, negativeNum
 * @param {Any} value to test.
 * @return {Boolean} true if 'value' is a number, false otherwise.
 */
is.negativeNumber = function (value) {
    return '[object Number]' === toString.call(value) && value < 0;
};
is.negNum = is.negativeNum = is.negativeNumber;

/**
 * Test if 'value' is a decimal number.
 * Aliases: decimalNumber, decNum
 * @param {Any} value value to test.
 * @return {Boolean} true if 'value' is a decimal number, false otherwise.
 */
is.decimal = function (value) {
    return '[object Number]' === toString.call(value) && value % 1 !== 0;
};
is.decNum = is.decNumer = is.decimal;

/**
 * Test if 'value' is divisible by 'n'.
 * Alias: divisBy
 * @param {Number} value value to test.
 * @param {Number} n dividend.
 * @return {Boolean} true if 'value' is divisible by 'n', false otherwise.
 */
is.divisibleBy = function (value, n) {
    if (value === 0)
        return false;
    return '[object Number]' === toString.call(value) &&
        n !== 0 &&
        value % n === 0;
};
is.divisBy = is.divisibleBy;

/**
 * Test if 'value' is an integer.
 * Alias: integer
 * @param {Any} value to test.
 * @return {Boolean} true if 'value' is an integer, false otherwise.
 */
is.int = function (value) {
    return '[object Number]' === toString.call(value) && value % 1 === 0;
};
is.integer = is.int;

/**
 * Test if 'value' is a positive integer.
 * Alias: posInt
 * @param {Any} value to test.
 * @return {Boolean} true if 'value' is a positive integer, false otherwise.
 */
is.positiveInt = function (value) {
    return '[object Number]' === toString.call(value) && value % 1 === 0 && value > 0;
};
is.posInt = is.positiveInteger = is.positiveInt;

/**
 * Test if 'value' is a negative integer.
 * Aliases: negInt, negativeInteger
 * @param {Any} value to test.
 * @return {Boolean} true if 'value' is a negative integer, false otherwise.
 */
is.negativeInt = function (value) {
    return '[object Number]' === toString.call(value) && value % 1 === 0 && value < 0;
};
is.negativeInteger = is.negInt = is.negativeInt;

/**
 * Test if 'value' is greater than 'others' values.
 * Alias: max
 * @param {Number} value value to test.
 * @param {Array} others values to compare with.
 * @return {Boolean} true if 'value' is greater than 'others' values.
 */
is.maximum = function (value, others) {
    if (!is.arrayLike(others) || !is.number(value))
        return false;

    var len = others.length;
    while (--len > -1) {
        if (value < others[len]) {
            return false;
        }
    }

    return true;
};
is.max = is.maximum;

/**
 * Test if 'value' is less than 'others' values.
 * Alias: min
 * @param {Number} value value to test.
 * @param {Array} others values to compare with.
 * @return {Boolean} true if 'value' is less than 'others' values.
 */
is.minimum = function (value, others) {
    if (!is.arrayLike(others) || !is.number(value))
        return false;

    var len = others.length;
    while (--len > -1) {
        if (value > others[len]) {
            return false;
        }
    }

    return true;
};
is.min = is.minimum;

/**
 * is.nan
 * Test if `value` is not a number.
 *
 * @param {Mixed} value value to test
 * @return {Boolean} true if `value` is not a number, false otherwise
 * @api public
 */
is.nan = function (value) {
    return !is.num(value) || value !== value;
};
is.notANumber = is.notANum = is.nan;

/**
 * Test if 'value' is an even number.
 * @param {Number} value to test.
 * @return {Boolean} true if 'value' is an even number, false otherwise.
 */
is.even = function (value) {
    return '[object Number]' === toString.call(value) && value % 2 === 0;
};

/**
 * Test if 'value' is an odd number.
 * @param {Number} value to test.
 * @return {Boolean} true if 'value' is an odd number, false otherwise.
 */
is.odd = function (value) {
    return !is.decimal(value) && '[object Number]' === toString.call(value) && value % 2 !== 0;
};

/**
 * Test if 'value' is greater than or equal to 'other'.
 * Aliases: greaterOrEq, greaterOrEqual
 * @param {Number} value value to test.
 * @param {Number} other value to compare with.
 * @return {Boolean} true, if value is greater than or equal to other, false otherwise.
 */
is.ge = function (value, other) {
    return value >= other;
};

/**
 * Test if 'value' is greater than 'other'.
 * Aliases: greaterThan
 * @param {Number} value value to test.
 * @param {Number} other value to compare with.
 * @return {Boolean} true, if value is greater than other, false otherwise.
 */
is.gt = function (value, other) {
    return value > other;
};
is.greaterThan = is.gt;

/**
 * Test if 'value' is less than or equal to 'other'.
 * Alias: lessThanOrEq, lessThanOrEqual
 * @param {Number} value value to test
 * @param {Number} other value to compare with
 * @return {Boolean} true, if 'value' is less than or equal to 'other', false otherwise.
 */
is.le = function (value, other) {
    return value <= other;
};
is.lessThanOrEq = is.lessThanOrEqual = is.le;

/**
 * Test if 'value' is less than 'other'.
 * Alias: lessThan
 * @param {Number} value value to test
 * @param {Number} other value to compare with
 * @return {Boolean} true, if 'value' is less than 'other', false otherwise.
 */
is.lt = function (value, other) {
    return value < other;
};
is.lessThan = is.lt;

/**
 * Test if 'value' is within 'start' and 'finish'.
 * Alias: withIn
 * @param {Number} value value to test.
 * @param {Number} start lower bound.
 * @param {Number} finish upper bound.
 * @return {Boolean} true if 'value' is is within 'start' and 'finish', false otherwise.
 */
is.within = function (value, start, finish) {
    return value >= start && value <= finish;
};
is.withIn = is.within;

/**
 * Test if 'value' is an object. Note: Arrays, RegExps, Date, Error, etc all return false.
 * Alias: obj
 * @param {Any} value to test.
 * @return {Boolean} true if 'value' is an object, false otherwise.
 */
is.object = function (value) {
    return '[object Object]' === toString.call(value);
};
is.obj = is.object;

/**
 * Test if 'value' is an object with properties. Note: Arrays are objects.
 * Alias: nonEmptyObj
 * @param {Any} value to test.
 * @return {Boolean} true if 'value' is an object, false otherwise.
 */
is.nonEmptyObject = function (value) {
    return '[object Object]' === toString.call(value) && Object.keys(value).length;
};
is.nonEmptyObj = is.nonEmptyObject;

/**
 * Test if 'value' is an instance type objType.
 * Aliases: objInstOf, objectinstanceof, instOf, instanceOf
 * @param {object} objInst an object to testfor type.
 * @param {object} objType an object type to compare.
 * @return {Boolean} true if 'value' is an object, false otherwise.
 */
is.objectInstanceof = function (objInst, objType) {
    try {
        return '[object Object]' === toString.call(objInst) && (objInst instanceof objType);
    } catch(err) {
        return false;
    }
};
is.instOf = is.instanceOf = is.objInstOf = is.objectInstanceOf = is.objectInstanceof;

/**
 * Test if 'value' is a regular expression.
 * Alias: regexp
 * @param {Any} value to test.
 * @return {Boolean} true if 'value' is a regexp, false otherwise.
 */
is.regexp = function (value) {
    return '[object RegExp]' === toString.call(value);
};
is.regExp = is.regexp;

/**
 * Test if 'value' is a string.
 * Alias: str
 * @param {Any} value to test.
 * @return {Boolean} true if 'value' is a string, false otherwise.
 */
is.string = function (value) {
    return '[object String]' === toString.call(value);
};
is.str = is.string;

/**
 * Test if 'value' is an empty string.
 * Alias: emptyStr
 * @param {Any} value to test.
 * @return {Boolean} true if 'value' is am empty string, false otherwise.
 */
is.emptyString = function (value) {
    return is.string(value) && value.length === 0;
};
is.emptyStr = is.emptyString;

/**
 * Test if 'value' is a non-empty string.
 * Alias: nonEmptyStr
 * @param {Any} value to test.
 * @return {Boolean} true if 'value' is a non-empty string, false otherwise.
 */
is.nonEmptyString = function (value) {
    return is.string(value) && value.length > 0;
};
is.nonEmptyStr = is.nonEmptyString;

/**
 * Test if value is a valid email address.
 * @param {Any} value to test if an email address.
 * @return {Boolean} true if an email address, false otherwise.
 */
is.emailAddress = function(value) {
    if (!is.nonEmptyStr(value))
        return false;
    return emailRegexp.test(value);
};
is.email = is.emailAddr = is.emailAddress;
var emailRegexp = /^([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x22([^\x0d\x22\x5c\x80-\xff]|\x5c[\x00-\x7f])*\x22)(\x2e([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x22([^\x0d\x22\x5c\x80-\xff]|\x5c[\x00-\x7f])*\x22))*\x40([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x5b([^\x0d\x5b-\x5d\x80-\xff]|\x5c[\x00-\x7f])*\x5d)(\x2e([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x5b([^\x0d\x5b-\x5d\x80-\xff]|\x5c[\x00-\x7f])*\x5d))*$/;

/**
 * Test if a value is either an IPv4 numeric IP address.
 * The rules are:
 * must be a string
 * length must be 15 characters or less
 * There must be four octets separated by a '.'
 * No octet can be less than 0 or greater than 255.
 * @param {Any} value to test if an ip address.
 * @return {Boolean} true if an ip address, false otherwise.
 */
is.ipv4Address = function(value) {
    if (!is.nonEmptyStr(value))  return false;
    if (value.length > 15)  return false;
    var octets = value.split('.');
    if (!is.array(octets) || octets.length !== 4)  return false;
    for (var i=0; i<octets.length; i++) {
        var val = parseInt(octets[i], 10);
        if (isNaN(val))  return false;
        if (val < 0 || val > 255)  return false;
    }
    return true;
};
is.ipv4 = is.ipv4Addr = is.ipv4Address;

/**
 * Test if a value is either an IPv6 numeric IP address.
 * @param {Any} value to test if an ip address.
 * @return {Boolean} true if an ip address, false otherwise.
 */
is.ipv6Address = function(value) {
    if (!is.nonEmptyStr(value))  return false;
    return ipv6RegExp.test(value);
};
is.ipv6 = is.ipv6Addr = is.ipv6Address;
var ipv6RegExp = /^((?=.*::)(?!.*::.+::)(::)?([\dA-F]{1,4}:(:|\b)|){5}|([\dA-F]{1,4}:){6})((([\dA-F]{1,4}((?!\3)::|:\b|$))|(?!\2\3)){2}|(((2[0-4]|1\d|[1-9])?\d|25[0-5])\.?\b){4})$/;

/**
 * Test if a value is either an IPv4 or IPv6 numeric IP address.
 * @param {Any} value to test if an ip address.
 * @return {Boolean} true if an ip address, false otherwise.
 */
is.ipAddress = function(value) {
    if (!is.nonEmptyStr(value)) return false;
    return is.ipv4(value) || is.ipv6(value);
};
is.ip = is.ip = is.ipAddress;

/**
 * Test if a value is a valid DNS address. eg www.stdarg.com is true while
 * 127.0.0.1 is false.
 * @param {Any} value to test if a DNS address.
 * @return {Boolean} true if a DNS address, false otherwise.
 * DNS Address is made up of labels separated by '.'
 * Each label must be between 1 and 63 characters long
 * The entire hostname (including the delimiting dots) has a maximum of 255 characters.
 * Hostname may not contain other characters, such as the underscore character (_)
 * oTher DNS names may contain the underscore.
 */
is.dnsAddress = function(value) {
    if (!is.nonEmptyStr(value))  return false;
    if (value.length > 255)  return false;
    var names = value.split('.');
    if (!is.array(names) || !names.length)  return false;
    if (names[0].indexOf('_') > -1)  return false;
    for (var i=0; i<names.length; i++) {
        if (!dnsLabel.test(names[i]))  return false;
    }
    return true;
};
is.dnsAddr = is.dns = is.dnsAddress;
var dnsLabel = /^(?![0-9]+$)(?!.*-$)(?!-)[a-zA-Z0-9-]{1,63}$/;

/**
 * Test is a value is a valid ipv4, ipv6 or DNS name.
 * @param {Any} value to test if a host address.
 * @return {Boolean} true if a host address, false otherwise.
 */
is.hostAddress = function(value) {
    if (!is.nonEmptyStr(value)) return false;
    return is.dns(value) || is.ipv4(value) || is.ipv6(value);
};
is.hostIp = is.hostAddr = is.hostAddress;

/**
 * Test if a number is a valid TCP port
 * @param {Any} value to test if its a valid TCP port
 */
is.port = function(value) {
    if (!is.positiveInt(value) || value > 65535)
        return false;
    return true;
};

