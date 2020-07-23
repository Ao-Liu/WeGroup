package edu.rosehulman.wegroup0

data class Friend(
    var username: String,
    var nickname: String,
    var isCandidate: Boolean = false,
    var text: String,
    var index: Int
){
    fun getInitial(): String{
        return username.substring(0, 1)
    }
}