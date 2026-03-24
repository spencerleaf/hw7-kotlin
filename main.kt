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
    right is MathExpressions.Negative -> simplifySub(left, right)
    else -> MathExpressions.Add(left, right)
}

fun simplifySub(left: MathExpressions, right: MathExpressions): MathExpressions = when{
    left is MathExpressions.Num && right is MathExpressions.Num -> MathExpressions.Num(left.value - right.value)
    right is MathExpressions.Num && right.value == 0 -> left
    left == right -> MathExpressions.Num(0)
    right is MathExpressions.Negative -> simplifyAdd(left, right)
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
    else -> MathExpressions.Div(left, right)
}

fun simplifyPow(base: MathExpressions, exp: MathExpressions): MathExpressions = when{
    exp is MathExpressions.Num && exp.value == 0 -> MathExpressions.Num(1)
    exp is MathExpressions.Num && exp.value == 1 -> base
    base is MathExpressions.Num && base.value == 0 -> MathExpressions.Num(0)
    base is MathExpressions.Num && base.value == 1 -> MathExpressions.Num(1)
    base is MathExpressions.Num && exp is MathExpressions.Num -> MathExpressions.Num(Math.pow(base.value.toDouble(), exp.value.toDouble()).toInt())
    else -> MathExpressions.Pow(base, exp)
}

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
}