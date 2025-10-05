package com.example.brigadeapp.domain.model

class ReportBuilder {
    private var type: String = ""
    private var place: String = ""
    private var time: String? = null
    private var description: String = ""
    private var followUp: Boolean = false
    private var imageUri: String? = null
    private var audioUri: String? = null

    fun setType(value: String) = apply { this.type = value }
    fun setPlace(value: String) = apply { this.place = value }
    fun setTime(value: String?) = apply { this.time = value }
    fun setDescription(value: String) = apply { this.description = value }
    fun setFollowUp(value: Boolean) = apply { this.followUp = value }
    fun setImageUri(value: String?) = apply { this.imageUri = value }
    fun setAudioUri(value: String?) = apply { this.audioUri = value }

    fun build(): Report {
        require(type.isNotEmpty()) { "Emergency type is required" }
        require(place.isNotEmpty()) { "Place is required" }
        require(description.isNotEmpty()) { "Description is required" }

        return Report(
            type = type,
            place = place,
            time = time,
            description = description,
            followUp = followUp,
            imageUri = imageUri,
            audioUri = audioUri
        )
    }
}