/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.nowinandroid.feature.interests.impl

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme
import java.io.File
import java.io.FileOutputStream

/**
 * Activity opened when the "IO connect Workshop" topic is tapped on the Interests screen.
 */
class FilterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NiaTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.errorContainer,
                            shadowElevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 40.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Bitmap Filters: Bad Practice",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    FilterScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                            .windowInsetsPadding(
                                WindowInsets.safeDrawing.only(
                                    WindowInsetsSides.Horizontal,
                                ),
                            )
                    )

                }
            }
        }
    }
}

class ImageFilter(val name: String, val matrix: FloatArray)


val FILTERS = listOf(
    ImageFilter(
        "Original", floatArrayOf(
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    ),
    ImageFilter(
        "Grayscale", floatArrayOf(
            0.299f, 0.587f, 0.114f, 0f, 0f,
            0.299f, 0.587f, 0.114f, 0f, 0f,
            0.299f, 0.587f, 0.114f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    ),
    ImageFilter(
        "Sepia", floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    ),
    ImageFilter(
        "Invert", floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        )
    ),
    ImageFilter(
        "Warm", floatArrayOf(
            1.15f, 0f, 0f, 0f, 10f,
            0f, 1.05f, 0f, 0f, 5f,
            0f, 0f, 0.85f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    ),
    ImageFilter(
        "Cool", floatArrayOf(
            0.85f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1.15f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        )
    ),
    ImageFilter(
        "Contrast", floatArrayOf(
            1.5f, 0f, 0f, 0f, -64f,
            0f, 1.5f, 0f, 0f, -64f,
            0f, 0f, 1.5f, 0f, -64f,
            0f, 0f, 0f, 1f, 0f
        )
    )
)

@Composable
fun FilterScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf(FILTERS.first()) }

    // Input bitmap for the filter pipeline (downsampled from 4000x4000 so the demo
    // does not OOM immediately -- the allocation churn below is the point).
    val sourceBitmap = remember { decodeSourceBitmap(context) }

    // BAD PRACTICE: a brand-new full-size filtered bitmap is allocated for the
    // preview on EVERY recomposition. Nothing is remembered, cached or reused,
    // so tapping through filters (or any state change) keeps allocating
    // multi-megabyte bitmaps and throwing the old ones to the GC.
    val previewBitmap = createFilteredBitmap(sourceBitmap, selectedFilter)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Preview: ${selectedFilter.name}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Image(
            bitmap = previewBitmap.asImageBitmap(),
            contentDescription = "Filtered preview (${selectedFilter.name})",
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Filters",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(FILTERS, key = { it.name }) { filter ->
                // BAD PRACTICE: each visible list item allocates its own FULL-SIZE
                // filtered bitmap just to render an 84dp thumbnail, and it is
                // re-created from scratch every time the item scrolls back into
                // view or the row recomposes.
                val thumbnailBitmap = createFilteredBitmap(sourceBitmap, filter)
                FilterThumbnail(
                    bitmap = thumbnailBitmap,
                    name = filter.name,
                    selected = filter == selectedFilter,
                    onClick = { selectedFilter = filter }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // BAD PRACTICE: yet another full-size bitmap is baked for sharing,
                // even though an identical preview bitmap was just created above.
                val bitmapToShare = createFilteredBitmap(sourceBitmap, selectedFilter)
                shareBitmap(context, bitmapToShare, selectedFilter.name)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Share filtered image")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun FilterThumbnail(
    bitmap: Bitmap,
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/** Decodes the bundled 4000x4000 image downsampled to roughly 1000x1000 (~4MB). */
fun decodeSourceBitmap(context: Context): Bitmap {
    val options = BitmapFactory.Options().apply { inSampleSize = 4 }
    return BitmapFactory.decodeResource(context.resources, R.drawable.large_image, options)
}

/**
 * Bakes [filter] into a brand-new [Bitmap] the same size as [source].
 * Each call allocates a fresh ARGB_8888 bitmap (~4MB for a 1000x1000 source).
 */
fun createFilteredBitmap(source: Bitmap, filter: ImageFilter): Bitmap {
    val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val paint = Paint(Paint.FILTER_BITMAP_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(ColorMatrix(filter.matrix))
    }
    canvas.drawBitmap(source, 0f, 0f, paint)
    return result
}

/** Writes [bitmap] to the share cache and fires an ACTION_SEND chooser for other apps. */
fun shareBitmap(context: Context, bitmap: Bitmap, filterName: String) {
    val shareDir = File(context.cacheDir, "shared_images").apply { mkdirs() }
    val file = File(shareDir, "filtered_${filterName.lowercase()}.png")
    FileOutputStream(file).use { stream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share filtered image"))
}

internal const val IO_CONNECT_WORKSHOP_TOPIC_NAME = "IO connect Workshop"