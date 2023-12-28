package com.github.kikimanjaro.intellify.models

class Playlist(var id: String, var name: String, var coverImage: String, var items: Array<String>) {

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }

    class Builder {
        private lateinit var id: String
        private lateinit var name: String
        private lateinit var coverImage: String
        private var items: Array<String> = emptyArray()

        fun setId(id: String): Builder {
            this.id = id
            return this
        }

        fun setName(name: String): Builder {
            this.name = name
            return this
        }

        fun setCoverImage(coverImage: String): Builder {
            this.coverImage = coverImage
            return this
        }

        fun setItems(items: Array<String>): Builder {
            this.items = items
            return this
        }

        fun addItem(item: String): Builder {
            this.items = this.items.plus(item)
            return this
        }

        fun build(): Playlist {
            return Playlist(id, name, coverImage, items)
        }
    }
}