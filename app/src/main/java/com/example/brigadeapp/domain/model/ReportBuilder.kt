package com.example.brigadeapp.domain.model

class ReportBuilder {
    private var type: String = ""
    private var place: String = ""
    private var time: String? = null
    private var description: String = ""
    private var followUp: Boolean = false
    private var imageUrl: String? = null
    private var audioUrl: String? = null

    fun setType(value: String) = apply { this.type = value }
    fun setPlace(value: String) = apply { this.place = value }
    fun setTime(value: String?) = apply { this.time = value }
    fun setDescription(value: String) = apply { this.description = value }
    fun setFollowUp(value: Boolean) = apply { this.followUp = value }
    fun setImageUrl(value: String?) = apply { this.imageUrl = value }
    fun setAudioUrl(value: String?) = apply { this.audioUrl = value }

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
            imageUrl = imageUrl,
            audioUrl = audioUrl
        )
    }
}