data class Set(
    val repetitions: Int,
    var isCompleted: Boolean = false
) {
    fun markAsCompleted() {
        isCompleted = true
    }
}