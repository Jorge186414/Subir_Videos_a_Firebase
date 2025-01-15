package com.tesji.videos_a_firebase

class ModeloVideo {

    var id : String ?= null

    var titulo : String ?= null
    var autor : String ?= null
    var videoUri : String ?= null
    var tiempo : String ?= null

    constructor()

    constructor(id: String?, titulo: String?, tiempo: String?, videoUri: String?, autor: String?) {
        this.id = id
        this.titulo = titulo
        this.tiempo = tiempo
        this.videoUri = videoUri
        this.autor = autor
    }

}