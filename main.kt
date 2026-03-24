sealed class MathExpressions{
    data class Num(val value: Int) : MathExpressions()
    data class Variable(val name: String) : MathExpressions()
    data class Negative(val name: MathExpressions) : MathExpressions()
    data class Add(val left: MathExpressions, val right: MathExpressions) : MathExpressions()
    data class Sub(val left: MathExpressions, val right: MathExpressions) : MathExpressions()
    data class Mul(val left: MathExpressions, val right: MathExpressions) : MathExpressions()
    data class Div(val left: MathExpressions, val right: MathExpressions) : MathExpressions()
    data class Pow(val base: MathExpressions, val exp: MathExpressions) : MathExpressions()
}

class Parser(val input: String){
    //ew im getting grin token flashbacks rn
    var position = 0
    val len = input.length
    fun noWhitespace(){
        //i know this should be while i just wanted to see if i could write a for loop
        for(i in position until len){
            if(input[i] != ' '){
                break
            }
            position++
        }
    }

    fun parseExpression(): MathExpressions{
        var left = parseTerm()
        noWhitespace()
        while(position < len && (input[position] == '+' || input[position] == '-')){
            val operator = input[position]
            position++
            noWhitespace()
            val right = parseTerm()
            if(operator == '+'){
                left = MathExpressions.Add(left, right)
            } else {
                left = MathExpressions.Sub(left, right)
            }
            noWhitespace()
        }
        return left
    }

    fun parseTerm(): MathExpressions{
        var left = parsePower()
        noWhitespace()
        while(position < len && (input[position] == '*' || input[position] == '/')){
            val operator = input[position]
            position++
            noWhitespace()
            val right = parsePower()
            if(operator == '*'){
                left = MathExpressions.Mul(left, right)
            } else {
                left = MathExpressions.Div(left, right)
            }
            noWhitespace()
        }
        return left
    }

    fun parsePower(): MathExpressions{
        val base = parseModifiers()
        noWhitespace()
        return when{
            position < len && input[position] == '^' ->{
                position++
                noWhitespace()
                val exp = parsePower()
                MathExpressions.Pow(base, exp)
            }
            else -> base
        }
    }

    fun parseModifiers(): MathExpressions{
        noWhitespace()
        if(position < len && input[position] == '-'){
            position++
            return MathExpressions.Negative(parseModifiers())
        }
        if(position < len && input[position] == '('){
            position++
            val expression = parseExpression()
            noWhitespace()
            position++
            return expression
        }
        if (position < len && input[position].isDigit()) {
            val start = position
            while (position < len && input[position].isDigit()){ 
                position++
            }
            return MathExpressions.Num(input.substring(start, position).toInt())
        }
        if (position < len && input[position].isLetter()) {
            val start = position
            while (position < len && input[position].isLetter()) {
                position++
            }
            return MathExpressions.Variable(input.substring(start, position))
        }
        throw Exception("unexpected character: ${input[position]}")
    }
}

fun parse(input: String): MathExpressions{
    val grinParserLol = Parser(input)
    return grinParserLol.parseExpression()
}

fun interpretFunction(line: String): String {
    return when {
        line.startsWith("deriv(") -> {
            val expr = line.removePrefix("deriv(").substringBefore(",")
            format(simpFix(simpFix(deriv(simpFix(parse(expr))))))
        }
        line.startsWith("eval(") -> {
            val expr = line.removePrefix("eval(").substringBefore(",")
            eval(parse(expr)).toString()
        }
        line.startsWith("simplify(") -> {
            val expr = line.removePrefix("simplify(").substringBefore(",")
            format(simpFix(parse(expr)))
        }
        else -> "unknown command: $line"
    }
}

fun simpFix(e: MathExpressions): MathExpressions {
    val simplified = simplify(e)
    return if (simplified == e) e else simpFix(simplified)
}

fun eval(e: MathExpressions): Int = when(e) {
    is MathExpressions.Num -> e.value
    is MathExpressions.Variable -> throw Exception("trying to eval a variable")
    is MathExpressions.Negative -> -eval(e.name)
    is MathExpressions.Add -> eval(e.left) + eval(e.right)
    is MathExpressions.Sub -> eval(e.left) - eval(e.right)
    is MathExpressions.Mul -> eval(e.left) * eval(e.right)
    is MathExpressions.Div -> eval(e.left) / eval(e.right)
    is MathExpressions.Pow -> Math.pow(eval(e.base).toDouble(), eval(e.exp).toDouble()).toInt()
}

fun simplify(e: MathExpressions): MathExpressions = when(e){
    is MathExpressions.Num -> e
    is MathExpressions.Variable -> e
    is MathExpressions.Negative ->
    {
        val s = simplify(e.name)
        when (s){
            is MathExpressions.Num -> MathExpressions.Num(-s.value)
            is MathExpressions.Negative -> s.name
            else -> MathExpressions.Negative(s)
        }
    }
    is MathExpressions.Add -> simplifyAdd(simplify(e.left), simplify(e.right))
    is MathExpressions.Sub -> simplifySub(simplify(e.left), simplify(e.right))
    is MathExpressions.Mul -> simplifyMul(simplify(e.left), simplify(e.right))
    is MathExpressions.Div -> simplifyDiv(simplify(e.left), simplify(e.right))
    is MathExpressions.Pow -> simplifyPow(simplify(e.base), simplify(e.exp))
}

fun simplifyAdd(left: MathExpressions, right: MathExpressions): MathExpressions = when{
    left is MathExpressions.Num && right is MathExpressions.Num -> MathExpressions.Num(left.value + right.value)
    left is MathExpressions.Num && left.value == 0 -> right
    right is MathExpressions.Num && right.value == 0 -> left
    right is MathExpressions.Negative -> simplifySub(left, right.name)
    left is MathExpressions.Num && right is MathExpressions.Sub && right.left is MathExpressions.Num -> simplifySub(MathExpressions.Num(left.value + right.left.value), right.right)
    left is MathExpressions.Sub && left.left is MathExpressions.Num && right is MathExpressions.Num -> simplifySub(MathExpressions.Num(left.left.value + right.value), left.right)
    left is MathExpressions.Sub && right is MathExpressions.Num && right.value == 0 -> left
    right is MathExpressions.Sub && left is MathExpressions.Num && left.value == 0 -> right
    else -> MathExpressions.Add(left, right)
}

fun simplifySub(left: MathExpressions, right: MathExpressions): MathExpressions = when{
    left is MathExpressions.Num && right is MathExpressions.Num -> MathExpressions.Num(left.value - right.value)
    right is MathExpressions.Num && right.value == 0 -> left
    left == right -> MathExpressions.Num(0)
    right is MathExpressions.Negative -> simplifyAdd(left, right.name)
    right is MathExpressions.Div && right.left is MathExpressions.Num && right.left.value < 0 -> simplifyAdd(left, MathExpressions.Div(MathExpressions.Num(-right.left.value), right.right))
    left is MathExpressions.Num && left.value == 0 -> MathExpressions.Negative(right)
    else -> MathExpressions.Sub(left, right)
}

fun simplifyMul(left: MathExpressions, right: MathExpressions): MathExpressions = when{
    left is MathExpressions.Num && right is MathExpressions.Num -> MathExpressions.Num(left.value * right.value)
    left is MathExpressions.Num && left.value == 1 -> right
    right is MathExpressions.Num && right.value == 1 -> left
    left is MathExpressions.Num && left.value == 0 -> MathExpressions.Num(0)
    right is MathExpressions.Num && right.value == 0 -> MathExpressions.Num(0)
    left is MathExpressions.Num && right is MathExpressions.Mul && right.left is MathExpressions.Num -> simplifyMul(MathExpressions.Num(left.value * right.left.value), right.right)
    left is MathExpressions.Mul && left.left is MathExpressions.Num && right is MathExpressions.Num -> simplifyMul(MathExpressions.Num(left.left.value * right.value), left.right)
    else -> MathExpressions.Mul(left, right)
}

fun simplifyDiv(left: MathExpressions, right: MathExpressions): MathExpressions = when{
    right is MathExpressions.Num && right.value == 0 -> throw Exception("Division by zero")
    right is MathExpressions.Num && right.value == 1-> left
    left is MathExpressions.Num && left.value == 0 -> MathExpressions.Num(0)
    left == right -> MathExpressions.Num(1)
    left is MathExpressions.Num && right is MathExpressions.Num -> MathExpressions.Num(left.value/right.value)
    left is MathExpressions.Mul && left.left == right -> left.right
    left is MathExpressions.Mul && left.right == right -> left.left
    left is MathExpressions.Negative && right is MathExpressions.Num -> MathExpressions.Negative(simplifyDiv(left.name, right))
    right is MathExpressions.Div && right.left is MathExpressions.Negative -> simplifyAdd(left, MathExpressions.Div(right.left.name, right.right))
    left is MathExpressions.Mul && right is MathExpressions.Pow && left.right == right.base && right.exp is MathExpressions.Num -> simplifyDiv(left.left, MathExpressions.Pow(right.base, MathExpressions.Num(right.exp.value - 1)))
    right is MathExpressions.Pow && left is MathExpressions.Negative -> MathExpressions.Negative(simplifyDiv(left.name, right))
    else -> MathExpressions.Div(left, right)
}

fun simplifyPow(base: MathExpressions, exp: MathExpressions): MathExpressions = when{
    exp is MathExpressions.Num && exp.value == 0 -> MathExpressions.Num(1)
    exp is MathExpressions.Num && exp.value == 1 -> base
    base is MathExpressions.Num && base.value == 0 -> MathExpressions.Num(0)
    base is MathExpressions.Num && base.value == 1 -> MathExpressions.Num(1)
    base is MathExpressions.Num && exp is MathExpressions.Num -> MathExpressions.Num(Math.pow(base.value.toDouble(), exp.value.toDouble()).toInt())
    base is MathExpressions.Pow && exp is MathExpressions.Num && base.exp is MathExpressions.Num -> simplifyPow(base.base, MathExpressions.Num(base.exp.value * exp.value))
    else -> MathExpressions.Pow(base, exp)
}

fun deriv(expression: MathExpressions): MathExpressions = simplify(when(expression) {
    is MathExpressions.Num -> MathExpressions.Num(0)
    is MathExpressions.Variable -> if (expression.name == "x") MathExpressions.Num(1) else MathExpressions.Num(0)
    is MathExpressions.Negative -> MathExpressions.Negative(deriv(expression.name))
    is MathExpressions.Add -> MathExpressions.Add(deriv(expression.left), deriv(expression.right))
    is MathExpressions.Sub -> MathExpressions.Sub(deriv(expression.left), deriv(expression.right))
    is MathExpressions.Mul -> MathExpressions.Add(
        MathExpressions.Mul(deriv(expression.left), expression.right),
        MathExpressions.Mul(expression.left, deriv(expression.right))
    )
    is MathExpressions.Div -> MathExpressions.Div(
        MathExpressions.Sub(
            MathExpressions.Mul(deriv(expression.left), expression.right),
            MathExpressions.Mul(expression.left, deriv(expression.right))
        ),
        MathExpressions.Pow(expression.right, MathExpressions.Num(2))
    )
    is MathExpressions.Pow -> when {
        expression.exp is MathExpressions.Num -> MathExpressions.Mul(
            MathExpressions.Mul(expression.exp, MathExpressions.Pow(expression.base, MathExpressions.Num(expression.exp.value - 1))),
            deriv(expression.base)
        )
        else -> throw Exception("can't differentiate this yet")
    }
})

fun format(expression: MathExpressions): String = when(expression){
    is MathExpressions.Num -> expression.value.toString()
    is MathExpressions.Variable -> expression.name
    is MathExpressions.Negative -> "-(${format(expression.name)})"
    is MathExpressions.Add -> "${format(expression.left)} + ${format(expression.right)}"
    is MathExpressions.Sub -> "${format(expression.left)} - ${format(expression.right)}"
    is MathExpressions.Mul -> "${format(expression.left)} * ${format(expression.right)}"
    is MathExpressions.Div -> "${format(expression.left)} / ${format(expression.right)}"
    is MathExpressions.Pow -> "${format(expression.base)}^${format(expression.exp)}"
}

fun formatParens(expression: MathExpressions, prec: Int): String {
    val result = when(expression) {
        is MathExpressions.Num -> expression.value.toString()
        is MathExpressions.Variable -> expression.name
        is MathExpressions.Negative -> "-(${formatParens(expression.name, 0)})"
        is MathExpressions.Add -> "${formatParens(expression.left, 1)} + ${formatParens(expression.right, 1)}"
        is MathExpressions.Sub -> "${formatParens(expression.left, 1)} - ${formatParens(expression.right, 2)}"
        is MathExpressions.Mul -> "${formatParens(expression.left, 2)} * ${formatParens(expression.right, 2)}"
        is MathExpressions.Div -> "${formatParens(expression.left, 2)} / ${formatParens(expression.right, 3)}"
        is MathExpressions.Pow -> "${formatParens(expression.base, 3)}^${formatParens(expression.exp, 0)}"
    }
    val myPrec = when(expression) {
        is MathExpressions.Add, is MathExpressions.Sub -> 1
        is MathExpressions.Mul, is MathExpressions.Div -> 2
        is MathExpressions.Pow -> 3
        else -> 4
    }
    return if (myPrec < prec) "($result)" else result
}

fun formatBetter(expression: MathExpressions) = formatParens(expression, 0)

fun main(){
    val x = MathExpressions.Num(5)
    println(x)
    val expr = MathExpressions.Add(MathExpressions.Num(3), MathExpressions.Mul(MathExpressions.Num(2), MathExpressions.Num(4)))
    println(eval(expr))
    val expr2 = MathExpressions.Pow(MathExpressions.Num(2), MathExpressions.Num(3))
    println(eval(expr2))
    val expr3 = MathExpressions.Negative(MathExpressions.Num(5))
    println(eval(expr3))
    val expr4 = MathExpressions.Add(MathExpressions.Num(0), MathExpressions.Variable("x"))
    println(simplify(expr4))
    val expr5 = MathExpressions.Sub(MathExpressions.Variable("x"), MathExpressions.Variable("x"))
    println(simplify(expr5))
    val expr6 = MathExpressions.Mul(MathExpressions.Num(2), MathExpressions.Mul(MathExpressions.Num(3), MathExpressions.Variable("x")))
    println(simplify(expr6))
    val expr7 = MathExpressions.Pow(MathExpressions.Variable("x"), MathExpressions.Num(2))
    println(deriv(expr7))
    val expr8 = MathExpressions.Mul(MathExpressions.Num(3), MathExpressions.Variable("x"))
    println(deriv(expr8))
    val expr9 = MathExpressions.Add(
        MathExpressions.Pow(MathExpressions.Variable("x"), MathExpressions.Num(3)),
        MathExpressions.Mul(MathExpressions.Num(2), MathExpressions.Variable("x"))
    )
    println(deriv(expr9))
    println(format(MathExpressions.Num(5)))
    println(format(MathExpressions.Variable("x")))
    println(format(MathExpressions.Add(MathExpressions.Num(3), MathExpressions.Variable("x"))))
    println(format(MathExpressions.Mul(MathExpressions.Num(2), MathExpressions.Pow(MathExpressions.Variable("x"), MathExpressions.Num(3)))))
    println(format(deriv(MathExpressions.Pow(MathExpressions.Variable("x"), MathExpressions.Num(2)))))
    println(format(deriv(MathExpressions.Add(
    MathExpressions.Pow(MathExpressions.Variable("x"), MathExpressions.Num(3)), MathExpressions.Mul(MathExpressions.Num(2), MathExpressions.Variable("x"))))))
    println(format(parse("x^2")))                    
    println(format(parse("3 + 2*x")))               
    println(format(deriv(parse("x^2"))))           
    println(format(deriv(parse("x+x^2+x^3"))))
    println(interpretFunction("deriv(x^2, Y)"))
    println(interpretFunction("deriv(x+x^2+x^3, Y)"))
    println(interpretFunction("deriv((x*2*x)/x, Y)"))
    println(interpretFunction("deriv(x^4+2*x^3-x^2+5*x-1/x, Y)"))
    println(interpretFunction("deriv(4*x^3+6*x^2-2*x+5+1/x^2, Y)"))
    println(interpretFunction("eval(5-6*18/3+2, Y)"))
    println(interpretFunction("eval(10*20-9/3+20, Y)"))
    println(interpretFunction("eval(10^3*9-100, Y)"))
    println(interpretFunction("simplify(5-x*(3/3)+2, Y)"))
    println(interpretFunction("simplify(1*x-0/3+2, Y)"))
    println(interpretFunction("simplify(5+2*6+x, Y)"))

    println(formatBetter(parse("3+x")))           
    println(formatBetter(parse("(3+x)*2")))       
    println(formatBetter(parse("3+x*2")))         
    println(formatBetter(deriv(parse("x^2+3*x"))))
}