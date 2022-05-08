package com.example.sound.logic.dao

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContentResolverCompat.query
import com.example.sound.MyApplication
import com.example.sound.MyApplication.Companion.context
import com.example.sound.logic.audio.Audio
import com.example.sound.logic.audio.AudioService

object AudioDao {
    fun getAudioName(): ArrayList<Audio>{
        val audios = ArrayList<Audio>()

        // 获得对应的uri
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        // 要返回的内容
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE
        )

        // 查询的条件
        val selection = null
        val selectionArgs = null
//        val selection = "${MediaStore.Audio.Media.OWNER_PACKAGE_NAME} = ?"
//        val selectionArgs = arrayOf(AudioService.APP_FOLDER_NAME)
        // 排序方式
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        val query = context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        query?.use { cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val timeAddColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val timestamp = cursor.getInt(timeAddColumn)
                val duration = cursor.getInt(durationColumn)
                val size = cursor.getInt(sizeColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                audios.add(Audio(id, name, "", timestamp, duration, size))
            }
        }
        return audios
    }
}