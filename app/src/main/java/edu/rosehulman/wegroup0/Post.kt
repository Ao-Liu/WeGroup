package edu.rosehulman.wegroup0

data class Post(
    var owner: String,
    var text: String,
    //hash: username, comment
    var comment: HashMap<String, String>,
    var time: String,
    var likedBy: ArrayList<String>
) {
}