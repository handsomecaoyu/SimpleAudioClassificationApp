package com.example.sound.logic.dao

import android.annotation.SuppressLint
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.sound.MyApplication.Companion.context
import com.example.sound.logic.model.Audio
import java.text.SimpleDateFormat

object AudioDao {
    @SuppressLint("SimpleDateFormat")
    fun getAudioInfo(): ArrayList<Audio>{
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

        // 查询的条件设置为我们App的目录下的音频
        val selection = MediaStore.Audio.Media.DATA + " like ? "
        val selectionArgs = arrayOf("/%" + "mySoundApp" + "/%")
        // 排序方式
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        // 建立查询
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
                val dateAdd = cursor.getLong(timeAddColumn)
                val duration = cursor.getInt(durationColumn)
                val size = cursor.getInt(sizeColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                // 添加信息
                audios.add(Audio(
                    id,
                    name,
                    "",
                    dateAdd,
                        // 得到的日期时间单位是秒，但是日期转换的时候要乘以1000表示位毫秒
                    SimpleDateFormat("YYYY年M月d日_H点mm分ss秒").format(dateAdd * 1000),
                    SimpleDateFormat("mm:ss").format(duration),
                    size))
            }
        }
        return audios
    }
}