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
    is MathExpressions.Pow -> simplifyPow(simplify(e.left), simplify(e.right))
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
}