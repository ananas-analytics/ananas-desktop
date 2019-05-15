'use strict';
var assert = require('assert');
var is = require('../lib/is2');

describe('is.a()', function() {
    it('Should return true if value is equal to string type', function(){
        assert.equal(true, is.a('This is a test', 'string'));
        assert.equal(false, is.a('This is also a test', 'number'));
        // type is an alias
        assert.equal(true, is.type('This is a test', 'string'));
        assert.equal(false, is.type('This is also a test', 'number'));
    });
});

describe('is.defined()', function() {
    it('Should return true if value is not undefined', function() {
        var val1;
        assert.equal(false, is.defined(val1));
        assert.equal(true, is.defined(false));
    });
});

describe('is.nullOrUndefined', function() {
    it('Should return true if the value is either null or undefined', function () {
        assert.equal(true, is.nullOrUndefined(null));
        assert.equal(true, is.nullOrUndefined(undefined));
        assert.equal(false, is.nullOrUndefined(true));
    });
});

describe('is.empty', function() {
    it('Should return true if the value is a string, array or object and contains nothing', function() {
        assert.equal(true, is.empty(''));
        assert.equal(true, is.empty({}));
        assert.equal(true, is.empty([]));
        assert.equal(false, is.empty('a'));
        assert.equal(false, is.empty({a: true}));
        assert.equal(false, is.empty(['a']));
        assert.equal(false, is.empty(false));
        assert.equal(false, is.empty(0));
        assert.equal(false, is.empty(function() {}));
    });
});

describe('is.equal', function() {
    it('Should return true if value is the same as value1', function() {
        assert.equal(true, is.equal(true, true));
        assert.equal(true, is.equal(1, 1));
        assert.equal(true, is.equal('1', '1'));
        assert.equal(true, is.equal(['1'], ['1']));
        assert.equal(true, is.equal({a: '1'}, {a: '1'}));
        assert.equal(true, is.equal({a: '1', c: {b: true}}, {a: '1', c: {b: true}}));

        assert.equal(false, is.equal(true, false));
        assert.equal(false, is.equal(1, 0));
        assert.equal(false, is.equal('1', '2'));
        assert.equal(false, is.equal(['1'], ['0']));
        assert.equal(false, is.equal({a: '1'}, {a: '2'}));
        assert.equal(true, is.equal({a: false}, {a: false}));
        assert.equal(false, is.equal({a: '1', c: {b: true}}, {a: '1', c: {b: false}}));
    });
});

describe('is.hosted', function() {
    it('Should return true if value1 is hosted in value2', function() {
        assert.equal(false, is.hosted(true, [false, true]));
        assert.equal(false, is.hosted(true, [true, true]));
        assert.equal(false, is.hosted('a', [false, true, 'a']));
        assert.equal(false, is.hosted('b', [ false, true, 'a'] ));
        assert.equal(true, is.hosted(0, [{}, 2, 3]));
        assert.equal(true, is.hosted('a', { a: {} } ));
        assert.equal(false, is.hosted('b', 'This be a string'));
        assert.equal(false, is.hosted('x', 'This be a string'));
        assert.equal(true, is.hosted('x', { x: []}));
        assert.equal(true, is.hosted('x', { x: {}}));
        assert.equal(false, is.hosted('x', { x: true}));
    });
});

describe('is.instanceOf', function() {
    it('Should return true if value is an instance of constructor', function() {
        function Circle() {
            this.raidius = 3;
            this.area = 4;
        }
        function Polygon() {
            this.edges = 8;                    // octogons are the default
            this.regular = false;              // sides needn't be all the same
            this.area = 1;
        }

        function Rectangle(top_len, side_len) {
            this.edges = 4;
            this.top = top_len;
            this.side = side_len;
            this.area = top_len*side_len;
        }
        Rectangle.prototype = new Polygon();
        var box = new Rectangle(8,3);

        assert.equal(true, is.instanceOf(box, Rectangle));
        assert.equal(true, is.instanceOf(box, Polygon));
        assert.equal(false, is.instanceOf(box, Circle));
        assert.equal(false, is.instanceOf(box, undefined));
    });
});

describe('is.null', function() {
    it('Should return true if value is null', function() {
        assert.equal(false, is.null(undefined));
        assert.equal(true, is.null(null));
    });
});

describe('is.undefined', function() {
    it('Should return true if value is undefined', function() {
        assert.equal(true, is.undefined(undefined));
        assert.equal(false, is.undefined(null));
        assert.equal(false, is.undefined(false));
        assert.equal(true, is.undefined());
    });
});

describe('is.arguments', function() {
    it('Should return true if value is an arguments object', function() {
        assert.equal(true, is.arguments(arguments));
        assert.equal(false, is.arguments(['1', '2', '3', false]));
    });
});

describe('is.array', function() {
    it('Should return true if value is an array', function() {
        assert.equal(false, is.array(false));
        assert.equal(true, is.array([1,2,3]));
        assert.equal(false, is.array(arguments));
        assert.equal(false, is.array({1: 'a', 2: 'b'}));
    });
});

describe('is.nonEmptyArray', function() {
    it('Should return true if value is a non-empty array', function() {
        assert.equal(false, is.nonEmptyArray([]));
        assert.equal(false, is.nonEmptyArray({}));
        assert.equal(false, is.nonEmptyArray({a:1}));
        assert.equal(true, is.nonEmptyArray([1]));
        assert.equal(true, is.nonEmptyArray([1,2]));
        assert.equal(true, is.nonEmptyArray([1,2,3]));
    });
});

describe('is.empty', function() {
    it('Should return true if value is an empty array-like object', function() {
        assert.equal(true, is.empty({}));
        assert.equal(true, is.empty([]));
        assert.equal(true, is.empty(arguments));
        assert.equal(false, is.empty({a:1}));
        assert.equal(false, is.empty([2]));
    });
});

describe('is.arrayLike', function() {
    it('Should return true if value is an array-like object', function() {
        assert.equal(false, is.arrayLike(false));
        assert.equal(false, is.arrayLike(1));
        assert.equal(false, is.arrayLike(new Date()));
        assert.equal(false, is.arrayLike(new Error()));

        var f = function(arg1, arg2) {
            assert.equal(true, is.arrayLike(arguments));
        };
        f('test1', false);

        assert.equal(true, is.arrayLike(arguments));
        assert.equal(true, is.arrayLike([]));
        assert.equal(true, is.arrayLike([1]));
        assert.equal(true, is.arrayLike([1,2]));
        assert.equal(false, is.arrayLike({}));
        assert.equal(false, is.arrayLike({a:1}));
        assert.equal(false, is.arrayLike({a:1,b:2}));
    });
});

describe('is.false', function() {
    it('Should return true if value is false', function() {
        assert.equal(false, is.false(1));
        assert.equal(false, is.false(null));
        assert.equal(false, is.false());
        assert.equal(false, is.false('Hello'));
        assert.equal(false, is.false([]));
        assert.equal(false, is.false({}));
        assert.equal(false, is.false(true));
        assert.equal(true, is.false(false));
        assert.equal(true, is.false(1!==1));
        assert.equal(false, is.false(1===1));
    });
});

describe('is.true', function() {
    it('Should return true if value is true', function() {
        assert.equal(false, is.true(1));
        assert.equal(false, is.true(null));
        assert.equal(false, is.true());
        assert.equal(false, is.true('Hello'));
        assert.equal(false, is.true([]));
        assert.equal(false, is.true({}));
        assert.equal(true, is.true(true));
        assert.equal(false, is.true(false));
        assert.equal(false, is.true(1!==1));
        assert.equal(true, is.true(1===1));
    });
});

describe('is.date', function() {
    it('Should return true if value is a date object', function() {
        assert.equal(false, is.date());
        assert.equal(false, is.date(false));
        assert.equal(false, is.date({}));
        assert.equal(false, is.date(new Error()));
        assert.equal(true, is.date(new Date()));
        assert.equal(false, is.date([]));
    });
});

describe('is.error', function() {
    it('Should return true if value is an error object', function() {
        assert.equal(false, is.error());
        assert.equal(false, is.error(1));
        assert.equal(false, is.error([]));
        assert.equal(false, is.error([1]));
        assert.equal(false, is.error([1,2]));
        assert.equal(false, is.error({a:1}));
        assert.equal(false, is.error({a:1,b:2}));
        assert.equal(false, is.error({a:1,b:2,c:3}));
        assert.equal(false, is.error(false));
        assert.equal(false, is.error(null));
        assert.equal(false, is.error('error'));
        assert.equal(false, is.error(new Date()));
        assert.equal(true, is.error(new Error()));
    });
});

describe('is.function', function() {
    it('Should return true if value is a function', function() {
        assert.equal(false, is.function());
        assert.equal(false, is.function('a'));
        assert.equal(false, is.function(1));
        assert.equal(false, is.function(true));
        assert.equal(false, is.function(null));
        assert.equal(false, is.function(false));
        assert.equal(false, is.function({}));
        assert.equal(false, is.function({a:1}));
        assert.equal(false, is.function({a:1,b:2}));
        assert.equal(false, is.function([]));
        assert.equal(false, is.function([1]));
        assert.equal(false, is.function([1,2]));
        assert.equal(false, is.function(new Error()));
        assert.equal(false, is.function(new Date()));

        var f = function() { var a = 1; a++; };
        assert.equal(true, is.function(f));
    });
});

describe('is.number', function() {
    it('Should return true if value is a number', function() {
        assert.equal(false, is.number(false));
        assert.equal(false, is.number({}));
        assert.equal(false, is.number([]));
        assert.equal(false, is.number(new Error()));
        assert.equal(false, is.number(new Date()));
        assert.equal(false, is.number('hiya'));
        assert.equal(false, is.number(true));
        assert.equal(false, is.number());
        assert.equal(false, is.number(null));
        assert.equal(true, is.number(1));
        assert.equal(true, is.number(0));
        assert.equal(true, is.number(1.0000001));
        assert.equal(true, is.number(-1.0000001));
        assert.equal(true, is.number(-0));
        assert.equal(true, is.number(2/0));
        assert.equal(true, is.number(0/2));
    });
});

describe('is.positiveNumber', function() {
    it('Should return true if value is a positive number', function() {
        assert.equal(false, is.positiveNumber());
        assert.equal(false, is.positiveNumber(null));
        assert.equal(false, is.positiveNumber(-1));
        assert.equal(false, is.positiveNumber(0));
        assert.equal(false, is.positiveNumber('hello'));
        assert.equal(false, is.positiveNumber('1'));
        assert.equal(false, is.positiveNumber(new Date()));
        assert.equal(false, is.positiveNumber(new Error()));
        assert.equal(false, is.positiveNumber({}));
        assert.equal(false, is.positiveNumber(-1.1));
        assert.equal(true, is.positiveNumber(1));
        assert.equal(true, is.positiveNumber(1/2.00001));
        assert.equal(true, is.positiveNumber(0.00001));
    });
});


describe('is.negativeNumber', function() {
    it('Should return true if value is a negative number', function() {
        assert.equal(false, is.negativeNumber());
        assert.equal(false, is.negativeNumber(null));
        assert.equal(false, is.negativeNumber(0));
        assert.equal(false, is.negativeNumber('hello'));
        assert.equal(false, is.negativeNumber('1'));
        assert.equal(false, is.negativeNumber(new Date()));
        assert.equal(false, is.negativeNumber(new Error()));
        assert.equal(false, is.negativeNumber({}));
        assert.equal(true, is.negativeNumber(-1.1));
        assert.equal(true, is.negativeNumber(-1));
        assert.equal(true, is.negativeNumber(-1/2.00001));
        assert.equal(true, is.negativeNumber(-0.00001));
    });
});

describe('is.decimal', function() {
    it('Should return true if value is a decimal number (has a fractional value).', function() {
        assert.equal(false, is.decimal(null));
        assert.equal(false, is.decimal());
        assert.equal(false, is.decimal(false));
        assert.equal(false, is.decimal(true));
        assert.equal(false, is.decimal(1));
        assert.equal(false, is.decimal(-1));
        assert.equal(false, is.decimal(0));
        assert.equal(false, is.decimal(10));
        assert.equal(false, is.decimal(new Date()));
        assert.equal(false, is.decimal(new Error()));
        assert.equal(true, is.decimal(1.1));
        assert.equal(true, is.decimal(-1.1));
        assert.equal(true, is.decimal(0.000001));
        assert.equal(true, is.decimal(-0.000001));
        assert.equal(true, is.decimal(20.00002));
        assert.equal(true, is.decimal(-20.00002));
    });
});

describe('is.divisibleBy', function() {
    it('Should return true if value is divisible by n', function() {
        assert.equal(false, is.divisibleBy());
        assert.equal(false, is.divisibleBy(1));
        assert.equal(false, is.divisibleBy('Hello', 'there'));
        assert.equal(false, is.divisibleBy({}, {}));
        assert.equal(false, is.divisibleBy([],[]));
        assert.equal(false, is.divisibleBy(null,null));
        assert.equal(false, is.divisibleBy(1, 3));
        assert.equal(false, is.divisibleBy(0, 9));
        assert.equal(false, is.divisibleBy(-1, 3));
        assert.equal(false, is.divisibleBy(1, 2));
        assert.equal(true, is.divisibleBy(10, 2));
        assert.equal(true, is.divisibleBy(-10, -2));
        assert.equal(true, is.divisibleBy(-10, -1));
        assert.equal(true, is.divisibleBy(100, 10));
    });
});

describe('is.int', function() {
    it('Should return true if value is an integer', function() {
        assert.equal(false, is.integer(null));
        assert.equal(false, is.integer());
        assert.equal(false, is.integer('hello'));
        assert.equal(false, is.integer([]));
        assert.equal(false, is.integer({}));
        assert.equal(false, is.integer(new Error()));
        assert.equal(false, is.integer(new Date()));
        assert.equal(false, is.integer(false));
        assert.equal(false, is.integer(1.1));
        assert.equal(false, is.integer(0.1));
        assert.equal(false, is.integer(-0.0000001));
        assert.equal(false, is.integer(10000000.1));
        assert.equal(true, is.integer(0));
        assert.equal(true, is.integer(10));
        assert.equal(true, is.integer(-2));
        assert.equal(true, is.integer(-77));
    });
});

describe('is.positiveInt', function() {
    it('Should return true if value is a positive integer', function() {
        assert.equal(false, is.positiveInteger(null));
        assert.equal(false, is.positiveInteger());
        assert.equal(false, is.positiveInteger('hello'));
        assert.equal(false, is.positiveInteger([]));
        assert.equal(false, is.positiveInteger({}));
        assert.equal(false, is.positiveInteger(new Error()));
        assert.equal(false, is.positiveInteger(new Date()));
        assert.equal(false, is.positiveInteger(false));
        assert.equal(false, is.positiveInteger(1.1));
        assert.equal(false, is.positiveInteger(0.1));
        assert.equal(false, is.positiveInteger(-0.0000001));
        assert.equal(false, is.positiveInteger(10000000.1));
        assert.equal(false, is.positiveInteger(0));
        assert.equal(true, is.positiveInteger(10));
        assert.equal(false, is.positiveInteger(-2));
        assert.equal(true, is.positiveInteger(1));
    });
});

describe('is.negativeInt', function() {
    it('Should return true if value is a negative integer', function() {
        assert.equal(false, is.negativeInteger(null));
        assert.equal(false, is.negativeInteger());
        assert.equal(false, is.negativeInteger('hello'));
        assert.equal(false, is.negativeInteger([]));
        assert.equal(false, is.negativeInteger({}));
        assert.equal(false, is.negativeInteger(new Error()));
        assert.equal(false, is.negativeInteger(new Date()));
        assert.equal(false, is.negativeInteger(false));
        assert.equal(false, is.negativeInteger(1.1));
        assert.equal(false, is.negativeInteger(0.1));
        assert.equal(false, is.negativeInteger(-0.0000001));
        assert.equal(false, is.negativeInteger(10000000.1));
        assert.equal(false, is.negativeInteger(0));
        assert.equal(false, is.negativeInteger(10));
        assert.equal(true, is.negativeInteger(-2));
        assert.equal(true, is.negativeInteger(-20000));
        assert.equal(false, is.negativeInteger(1));
        assert.equal(false, is.negativeInteger(10000));
    });
});

describe('is.maximum', function() {
    it('Should return true if value is the maximum in the others array', function() {
        assert.equal(false, is.maximum(null,null));
        assert.equal(false, is.maximum('hello',null));
        assert.equal(false, is.maximum(1,null));
        assert.equal(false, is.maximum(false,true));
        assert.equal(false, is.maximum());
        assert.equal(false, is.maximum(null,[1,2,3,4,5]));
        assert.equal(false, is.maximum(true,[1,2,3,4,5]));
        assert.equal(false, is.maximum(undefined,[1,2,3,4,5]));
        assert.equal(false, is.maximum(new Date(),[1,2,3,4,5]));
        assert.equal(false, is.maximum(new Error(),[1,2,3,4,5]));
        assert.equal(false, is.maximum(1,[1,2,3,4,5]));
        assert.equal(false, is.maximum(2,[1,2,3,4,5]));
        assert.equal(false, is.maximum(3,[1,2,3,4,5]));
        assert.equal(false, is.maximum(4,[1,2,3,4,5]));
        assert.equal(true, is.maximum(5,[1,2,3,4,5]));
    });
});

describe('is.minimum', function() {
    it('Should return true if value is the minimum in the others array', function() {
        assert.equal(false, is.minimum(null,null));
        assert.equal(false, is.minimum('hello',null));
        assert.equal(false, is.minimum(1,null));
        assert.equal(false, is.minimum(false,true));
        assert.equal(false, is.minimum());
        assert.equal(false, is.minimum(null,[1,2,3,4,5]));
        assert.equal(false, is.minimum(true,[1,2,3,4,5]));
        assert.equal(false, is.minimum(undefined,[1,2,3,4,5]));
        assert.equal(false, is.minimum(new Date(),[1,2,3,4,5]));
        assert.equal(false, is.minimum(new Error(),[1,2,3,4,5]));
        assert.equal(true,  is.minimum(1,[1,2,3,4,5]));
        assert.equal(false, is.minimum(2,[1,2,3,4,5]));
        assert.equal(false, is.minimum(3,[1,2,3,4,5]));
        assert.equal(false, is.minimum(4,[1,2,3,4,5]));
        assert.equal(false, is.minimum(5,[1,2,3,4,5]));
    });
});

describe('is.nan', function() {
    it('Should return true if value is not a number', function() {
        assert.equal(true, is.nan(null));
        assert.equal(true, is.nan(undefined));
        assert.equal(true, is.nan(true));
        assert.equal(true, is.nan(false));
        assert.equal(false, is.nan(37));
        assert.equal(true, is.nan('37'));
        assert.equal(true, is.nan('37.37'));
        assert.equal(true, is.nan(' '));
        assert.equal(true, is.nan(''));        // false converted to 0
        assert.equal(true, is.nan('blabla'));
        assert.equal(true, is.nan(NaN));
    });
});

describe('is.even', function() {
    it('Should return true if value is an even integer', function() {
        assert.equal(false, is.even(null));
        assert.equal(false, is.even());
        assert.equal(false, is.even(new Date()));
        assert.equal(false, is.even('hello'));
        assert.equal(false, is.even(new Error()));
        assert.equal(false, is.even({}));
        assert.equal(false, is.even([]));
        assert.equal(false, is.even(23.000001));
        assert.equal(false, is.even(-2.000001));
        assert.equal(false, is.even(1));
        assert.equal(false, is.even(3));
        assert.equal(true, is.even(4));
        assert.equal(true, is.even(2));
        assert.equal(true, is.even(0));
        assert.equal(true, is.even(-2));
        assert.equal(true, is.even(10000));
    });
});

describe('is.odd', function() {
    it('Should return true if value is an odd integer', function() {
        assert.equal(false, is.odd(null));
        assert.equal(false, is.odd());
        assert.equal(false, is.odd(new Date()));
        assert.equal(false, is.odd('hello'));
        assert.equal(false, is.odd(new Error()));
        assert.equal(false, is.odd({}));
        assert.equal(false, is.odd([]));
        assert.equal(false, is.odd(23.000001));
        assert.equal(false, is.odd(-2.000001));
        assert.equal(false, is.odd(0));
        assert.equal(false, is.odd(2));
        assert.equal(true, is.odd(3));
        assert.equal(true, is.odd(1));
        assert.equal(true, is.odd(-1));
        assert.equal(true, is.odd(-3));
        assert.equal(true, is.odd(10001));
    });
});

describe('is.gt', function() {
    it('Should return true if value is greater than other', function() {
        assert.equal(false, is.gt());
        assert.equal(false, is.gt(null,null));
        assert.equal(false, is.gt('6', '7'));
        assert.equal(false, is.gt('alhpa', 'beta'));
        assert.equal(false, is.gt(6, 7));
        assert.equal(false, is.gt(6, 7));
    });
});

describe('is.ge', function() {
    it('Should return true if value is greater than or equal to other', function() {
        assert.equal(false, is.ge());
        assert.equal(true, is.ge(null,null));
        assert.equal(false, is.ge('6', '7'));
        assert.equal(false, is.ge('alhpa', 'beta'));
        assert.equal(false, is.ge(6, 7));
        assert.equal(true, is.ge(6, 6));
        assert.equal(true, is.ge(6, 5));
    });
});

describe('is.lt', function() {
    it('Should return true if value is less than other', function() {
        assert.equal(false, is.lt());
        assert.equal(false, is.lt(null,null));
        assert.equal(true, is.lt('6', '7'));
        assert.equal(true, is.lt('alhpa', 'beta'));
        assert.equal(true, is.lt(6, 7));
        assert.equal(false, is.lt(7, 6));
    });
});

describe('is.le', function() {
    it('Should return true if value is less than or equal to other', function() {
        assert.equal(false, is.le());
        assert.equal(true, is.le(null,null));
        assert.equal(true, is.le('6', '7'));
        assert.equal(true, is.le('6', '6'));
        assert.equal(true, is.le('alhpa', 'beta'));
        assert.equal(true, is.le(6, 7));
        assert.equal(true, is.le(6, 6));
        assert.equal(false, is.le(7, 6));
    });
});

describe('is.within', function() {
    it('Should return true if value is within start and finish values', function() {
        assert.equal(false, is.withIn(null, null));
        assert.equal(true, is.withIn(2, -1, 6));
        assert.equal(false, is.withIn(22, -1, 6));
        assert.equal(false, is.withIn('7', '2', '100')); // '7' is greater than '1'
        assert.equal(true, is.withIn(7, 2, 100));
        assert.equal(false, is.withIn('1', '2', '100'));
    });
});

describe('is.object', function() {
    it('Should return true if value is an object', function() {
        assert.equal(false, is.object(null));
        assert.equal(false, is.object(3));
        assert.equal(false, is.object(false));
        assert.equal(false, is.object(true));
        assert.equal(false, is.object(0));
        assert.equal(false, is.object('Hello'));
        assert.equal(false, is.object([]));
        assert.equal(true, is.object({}));
        assert.equal(false, is.object(new Error()));
        assert.equal(false, is.object(new Date()));
    });
});

describe('is.nonEmptyObject', function() {
    it('Should return true if value is an object with at least 1 property', function() {
        assert.equal(false, is.nonEmptyObject());
        assert.equal(false, is.nonEmptyObject(null));
        assert.equal(false, is.nonEmptyObject(7));
        assert.equal(false, is.nonEmptyObject(false));
        assert.equal(false, is.nonEmptyObject('Hello'));
        assert.equal(false, is.nonEmptyObject(new Error()));
        assert.equal(false, is.nonEmptyObject(new Date()));
        assert.equal(false, is.nonEmptyObject({}));
        assert.equal(true, is.nonEmptyObject({a:1}));
    });
});

describe('is.objectInstanceOf', function() {
    it('Should return true if value is an instance of type object', function() {
        function Circle() {
            this.raidius = 3;
            this.area = 4;
        }
        function Polygon() {
            this.edges = 8;                    // octogons are the default
            this.regular = false;              // sides needn't be all the same
            this.area = 1;
        }

        function Rectangle(top_len, side_len) {
            this.edges = 4;
            this.top = top_len;
            this.side = side_len;
            this.area = top_len*side_len;
        }
        Rectangle.prototype = new Polygon();
        var box = new Rectangle(8,3);

        assert.equal(true, is.objectInstanceOf(box, Rectangle));
        //assert.equal(true, is.objectInstanceOf(box, Polygon));
        assert.equal(false, is.objectInstanceOf(box, Circle));
        //assert.equal(false, is.objectInstanceOf(box, undefined));
    });
});

describe('is.regExp', function() {
    it('Should return true if value is a regular expression', function() {
        assert.equal(false, is.regExp(null));
        assert.equal(false, is.regExp(false));
        assert.equal(false, is.regExp(778));
        assert.equal(false, is.regExp([]));
        assert.equal(false, is.regExp({}));
        assert.equal(false, is.regExp('heya'));
        assert.equal(true, is.regExp(/is/g));
        assert.equal(true, is.regExp(new RegExp('e')));
    });
});

describe('is.string', function() {
    it('Should return true if value is a string', function() {
        assert.equal(false, is.string(null));
        assert.equal(false, is.string(false));
        assert.equal(false, is.string({}));
        assert.equal(false, is.string([]));
        assert.equal(false, is.string(9908));
        assert.equal(false, is.string(new RegExp('e')));
        assert.equal(false, is.string(new Date()));
        assert.equal(false, is.string(new Error()));
        assert.equal(true, is.string('hello'));
        assert.equal(true, is.string(''));
        assert.equal(true, is.string(String('cow')));
    });
});

describe('is.nonEmptyStr', function() {
    it('Should return true if value ', function() {
        assert.equal(false, is.nonEmptyStr());
        assert.equal(false, is.nonEmptyStr(null));
        assert.equal(false, is.nonEmptyStr(false));
        assert.equal(false, is.nonEmptyStr(8));
        assert.equal(false, is.nonEmptyStr(new Date()));
        assert.equal(false, is.nonEmptyStr(new Error()));
        assert.equal(false, is.nonEmptyStr(true));
        assert.equal(false, is.nonEmptyStr(new RegExp('e')));
        assert.equal(true, is.nonEmptyStr('heya'));
        assert.equal(false, is.nonEmptyStr(''));
        assert.equal(false, is.nonEmptyStr(String('')));
        assert.equal(true, is.nonEmptyStr(String('a')));
    });
});

describe('is.buffer', function() {
    it('Should return true if value ', function() {
        assert.equal(false, is.buffer());
        assert.equal(false, is.buffer(null));
        assert.equal(false, is.buffer(''));
        assert.equal(false, is.buffer(8));
        assert.equal(false, is.buffer(new Date()));
        assert.equal(false, is.buffer(new Error()));
        assert.equal(false, is.buffer(true));
        assert.equal(false, is.buffer(new RegExp('e')));
        assert.equal(true, is.buffer(new Buffer('heya')));
        assert.equal(false, is.buffer(''));
        assert.equal(false, is.buffer(String('')));
        assert.equal(true, is.buffer(new Buffer(23)));
    });
});


describe('is.emailAddress', function() {
    it('Should return true for valid email address ', function() {
        //http://isemail.info/_system/is_email/test/?all
        assert.equal(false, is.email('edmond'));
        assert.equal(true, is.email('edmond@stdarg'));
        assert.equal(true, is.email('edmond@stdarg.com'));
        assert.equal(true, is.email('edmond@127.0.0.1'));
        assert.equal(false, is.email('@'));
        assert.equal(false, is.email('@stdarg'));
        assert.equal(false, is.email('@stdarg.com'));
        assert.equal(false, is.email('@stdarg.something'));
        assert.equal(true, is.email('e@stdarg.something.something'));
        assert.equal(false, is.email('.e@stdarg.something'));
        assert.equal(true, is.email('e.m@stdarg.com'));
        assert.equal(false, is.email('e..m@stdarg.com'));
        assert.equal(true, is.email('!#$%&`*+/=?^`{|}~@stdarg.com'));
        //assert.equal(false, is.email('hi@edmond@stdarg.com'));
        //assert.equal(false, is.email('hi\\@edmond@stdarg.com'));
        //assert.equal(false, is.email('123@stdarg.com'));
        assert.equal(true, is.email('edmond@123.com'));
        assert.equal(true, is.email('abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghiklm@123.com'));

        //assert.equal(false, is.email('abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghiklmn@stdarg.com'));
        assert.equal(true, is.email('edmond@abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.com'));
        //assert.equal(false, is.email('edmond@abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghiklm.com'));
        assert.equal(true, is.email('edmond@test-stdarg.com'));
        //assert.equal(false, is.email('edmond@-stdarg.com'));
        assert.equal(true, is.email('edmond@test--stdarg.com'));
        assert.equal(false, is.email('edmond@.stdarg.com'));
        assert.equal(true, is.email('a@a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v.w.x.y.z.a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v.w.x.y.z.a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v.w.x.y.z.a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v.w.x.y.z.a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v'));
        assert.equal(true, is.email('abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghiklm@abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghi'));
        //assert.equal(false, is.email('abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghiklm@abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghikl.abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghij'));
        //assert.equal(false, is.email('"edmond"@stdarg.com'));
        //assert.equal(false, is.email('""@stdarg.com'));
        //assert.equal(false, is.email('"\\e"@stdarg.com'));


    });
});

describe('is.ipv4Address', function() {
    it('Should return true for valid ip address ', function() {
        assert.equal(false, is.ipv4('edmond'));
        assert.equal(false, is.ipv4('192.168.0.2000000000'));

        assert.equal(true, is.ipv4('192.168.0.2'));
        assert.equal(false, is.ipv4('336.332'));
        assert.equal(true, is.ipv4('255.255.255.0'));
        assert.equal(true, is.ipv4('255.255.255.255'));
        assert.equal(true, is.ipv4('0.0.0.0'));
        assert.equal(false, is.ipv4('192.168.a.0'));
    });
});

describe('is.ipv6Address', function() {
    it('Should return true for valid ip address ', function() {
        assert.equal(false, is.ipv6('edmond'));
        assert.equal(false, is.ipv6('192.168.0.2000000000'));

        assert.equal(false, is.ipv6('192.168.0.2'));
        assert.equal(false, is.ipv6('336.332'));
        assert.equal(false, is.ipv6(''));
        assert.equal(false, is.ipv6('---'));

        assert.equal(false, is.ipv6('2001:db8:3333:4444:5555:6666:1.2.3.4'));
        assert.equal(true, is.ipv6('2001:0000:1234:0000:0000:C1C0:ABCD:0876'));
        assert.equal(false, is.ipv6('2001:0000:1234:0000:0000:C1C0:ABCD:0876 0'));
        assert.equal(false, is.ipv6('2001:0000:1234: 0000:0000:C1C0:ABCD:0876'));
        assert.equal(false, is.ipv6('2001:1:1:1:1:1:255Z255X255Y255'));
        assert.equal(true, is.ipv6('2001:0:1234::C1C0:ABCD:876'));
        assert.equal(false, is.ipv6('3ffe:0b00:0000:0000:0001:0000:0000:000a'));
        assert.equal(false, is.ipv6('3ffe:b00::1:0:0:a'));
        assert.equal(true, is.ipv6('FF02:0000:0000:0000:0000:0000:0000:0001'));
        assert.equal(true, is.ipv6('FF02::1'));
        assert.equal(true, is.ipv6('0000:0000:0000:0000:0000:0000:0000:0001'));
        assert.equal(true, is.ipv6('0000:0000:0000:0000:0000:0000:0000:0000'));
        assert.equal(true, is.ipv6('::'));
        assert.equal(false, is.ipv6('::ffff:192.168.1.26')); // fix
        assert.equal(false, is.ipv6('02001:0000:1234:0000:0000:C1C0:ABCD:0876'));
        assert.equal(false, is.ipv6('2001:0000:1234:0000:00001:C1C0:ABCD:0876'));
        assert.equal(true, is.ipv6('2001:0:1234::C1C0:ABCD:876'));
        assert.equal(true, is.ipv6('2001:0000:1234:0000:0000:C1C0:ABCD:0876'));
        assert.equal(false, is.ipv6('2001:0000:1234:0000:0000:C1C0:ABCD:0876 0'));
        assert.equal(false, is.ipv6('2001:0000:1234: 0000:0000:C1C0:ABCD:0876'));
    });
});

describe('is.dnsAddress', function() {
    it('Should return true for valid dns address ', function() {
        assert.equal(true, is.dns('stdarg'));
        assert.equal(true, is.dns('stdarg.com'));
        assert.equal(true, is.dns('www.stdarg.com'));
        //assert.equal(false, is.dns('336.332'));
        assert.equal(true, is.dns('3stdarg.com'));
        assert.equal(false, is.dns('192.168.0.2000000000'));
        assert.equal(false, is.dns('192.168.0.2'));
        assert.equal(false, is.dns('*hi*.com'));
        assert.equal(false, is.dns('-hi-.com'));
        assert.equal(false, is.dns('_stdrg-.com'));
        assert.equal(true, is.dns('www--stdrg.com'));
        assert.equal(false, is.dns(':54:sda54'));
        assert.equal(false, is.dns('2001:db8:3333:4444:5555:6666:1.2.3.4'));
        assert.equal(false, is.dns('2001:0000:1234: 0000:0000:C1C0:ABCD:0876'));
        assert.equal(false, is.dns('2001:1:1:1:1:1:255Z255X255Y255'));
    });
});

describe('is.port', function() {
    it('Should return true for valid port numbers ', function() {
        assert.equal(true, is.port(1));
        assert.equal(true, is.port(10));
        assert.equal(true, is.port(100));
        assert.equal(true, is.port(65535));
    });
    it('Should return false for invalid port numbers ', function() {
        assert.equal(false, is.port(0));
        assert.equal(false, is.port(-10));
        assert.equal(false, is.port(-1100));
        assert.equal(false, is.port(65536));
        assert.equal(false, is.port());
        assert.equal(false, is.port(null));
        assert.equal(false, is.port('22'));
        assert.equal(false, is.port('heya'));
        assert.equal(false, is.port(true));
        assert.equal(false, is.port({a:22}));
        assert.equal(false, is.port([]));
        assert.equal(false, is.port([1,2,3]));
    });
});
