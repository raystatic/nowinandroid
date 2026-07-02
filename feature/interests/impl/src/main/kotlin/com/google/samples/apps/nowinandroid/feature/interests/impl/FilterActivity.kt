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
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

class ImageFilter(val name: String, val matrix: FloatArray) {
    val colorFilter: ColorFilter by lazy { ColorFilter.colorMatrix(ColorMatrix(matrix)) }
}

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
    val scope = rememberCoroutineScope()
    var selectedFilter by remember { mutableStateOf(FILTERS.first()) }
    var isSharing by remember { mutableStateOf(false) }

    // GOOD: the input bitmap is decoded exactly once and reused everywhere --
    // the preview, every thumbnail, and sharing all read from this single copy.
    val sourceBitmap = remember { decodeSourceBitmap(context) }
    val sourceImage: ImageBitmap = remember(sourceBitmap) { sourceBitmap.asImageBitmap() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Memory-Friendly Filtering",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "One bitmap lives in memory. Filters are rendered as draw-time " +
                        "ColorFilter overlays on that single bitmap, so switching filters " +
                        "or scrolling the thumbnail row allocates nothing. A filtered " +
                        "bitmap is baked only at the moment you share it to another app, " +
                        "and it is released as soon as it has been written to disk.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Preview: ${selectedFilter.name}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))

        // GOOD: the filter is a GPU draw-time overlay on the shared bitmap;
        // no new Bitmap is created when the selection changes.
        Image(
            bitmap = sourceImage,
            contentDescription = "Filtered preview (${selectedFilter.name})",
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray),
            contentScale = ContentScale.Crop,
            colorFilter = selectedFilter.colorFilter
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
                // GOOD: every thumbnail draws the same shared bitmap with its own
                // ColorFilter overlay -- zero extra bitmap allocations, no matter
                // how often the row scrolls or recomposes.
                FilterThumbnail(
                    image = sourceImage,
                    filter = filter,
                    selected = filter == selectedFilter,
                    onClick = { selectedFilter = filter }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (isSharing) return@Button
                isSharing = true
                scope.launch {
                    // The ONLY place a new bitmap is created: sharing needs real
                    // filtered pixels, so bake them off the main thread, write the
                    // file, then recycle the temporary bitmap immediately.
                    val uri = withContext(Dispatchers.IO) {
                        writeFilteredBitmapForSharing(context, sourceBitmap, selectedFilter)
                    }
                    isSharing = false
                    context.startActivity(buildShareIntent(uri))
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (isSharing) "Preparing image…" else "Share filtered image")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun FilterThumbnail(
    image: ImageBitmap,
    filter: ImageFilter,
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
                bitmap = image,
                contentDescription = filter.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = filter.colorFilter
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = filter.name,
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
 * Bakes [filter] into a temporary bitmap, writes it to the share cache, and
 * recycles the bitmap right away. This is the single point in the app where a
 * filtered bitmap is materialized, and it only lives long enough to hit disk.
 */
fun writeFilteredBitmapForSharing(context: Context, source: Bitmap, filter: ImageFilter): Uri {
    val filtered = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    try {
        val canvas = Canvas(filtered)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(android.graphics.ColorMatrix(filter.matrix))
        }
        canvas.drawBitmap(source, 0f, 0f, paint)

        val shareDir = File(context.cacheDir, "shared_images").apply { mkdirs() }
        val file = File(shareDir, "filtered_${filter.name.lowercase()}.png")
        FileOutputStream(file).use { stream ->
            filtered.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } finally {
        filtered.recycle()
    }
}

fun buildShareIntent(uri: Uri): Intent {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return Intent.createChooser(sendIntent, "Share filtered image")
}

internal const val IO_CONNECT_WORKSHOP_TOPIC_NAME = "IO connect Workshop"