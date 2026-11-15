package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.util.GeneratedPdfResult
import com.example.util.PdfExportService
import com.example.util.PdfRowData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Enterprise Print Preview Dialog for Financial Statements.
 * Displays high-fidelity A4 document pages rendered from the PDF engine before triggering
 * the Android Native System Print Dialog or Sharing sheet.
 */
@Composable
fun PrintPreviewDialog(
  result: GeneratedPdfResult,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val isKu = result.isKurdish

  var pageBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
  var isRendering by remember { mutableStateOf(true) }
  var selectedPageIndex by remember { mutableIntStateOf(0) }
  var zoomLevel by remember { mutableFloatStateOf(1.0f) }
  var showAllPagesScroll by remember { mutableStateOf(true) }

  // Render PDF pages to Bitmaps in background coroutine
  LaunchedEffect(result.file) {
    isRendering = true
    val bitmaps = withContext(Dispatchers.IO) {
      PdfExportService.renderPdfPagesToBitmaps(result.file, maxPages = result.totalPages.coerceAtLeast(1))
    }
    pageBitmaps = bitmaps
    isRendering = false
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
      usePlatformDefaultWidth = false,
      dismissOnBackPress = true,
      dismissOnClickOutside = false
    )
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .testTag("print_preview_dialog"),
      color = Color(0xFF090E17) // Executive Dark Desk
    ) {
      Column(
        modifier = Modifier.fillMaxSize()
      ) {
        // -------------------------------------------------------------
        // 1. Top Navigation & Action Header
        // -------------------------------------------------------------
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            IconButton(
              onClick = onDismiss,
              modifier = Modifier
                .size(38.dp)
                .background(Color(0xFF1E293B), CircleShape)
                .testTag("print_preview_close_action")
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Preview",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }

            Column {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = if (isKu) "پێشبینینا چاپێ" else "PRINT PREVIEW",
                    color = Color(0xFF10B981),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                  )
                }

                Text(
                  text = "A4 • ${result.totalPages} ${if (isKu) "بەرپەڕ" else "Pages"}",
                  color = Color(0xFF94A3B8),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }

              Text(
                text = result.documentTitle,
                color = Color.White,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 240.dp)
              )
            }
          }

          // Top Quick Print & Share Actions
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Share PDF Button
            OutlinedButton(
              onClick = {
                PdfExportService.sharePdfFile(context, result.file, result.documentTitle)
              },
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF38BDF8)
              ),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7)),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              modifier = Modifier
                .heightIn(min = 36.dp)
                .testTag("print_preview_quick_share_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share PDF",
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isKu) "پارڤەکرن" else "Share",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }

            // Print Statement Button (Triggers Android PrintManager)
            Button(
              onClick = {
                PdfExportService.printPdfFile(context, result.file, result.documentTitle)
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981),
                contentColor = Color(0xFF041E19)
              ),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              modifier = Modifier
                .heightIn(min = 36.dp)
                .testTag("print_preview_quick_print_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Print,
                contentDescription = "Print Document",
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isKu) "چاپکرن" else "Print",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
              )
            }
          }
        }

        // -------------------------------------------------------------
        // 2. View Mode & Zoom Toolbar
        // -------------------------------------------------------------
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF131D31))
            .padding(horizontal = 14.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Page Navigation (if multiple pages)
          if (result.totalPages > 1) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              IconButton(
                onClick = {
                  if (selectedPageIndex > 0) selectedPageIndex--
                },
                enabled = selectedPageIndex > 0,
                modifier = Modifier.size(30.dp)
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Previous Page",
                  tint = if (selectedPageIndex > 0) Color.White else Color(0xFF475569),
                  modifier = Modifier.size(16.dp)
                )
              }

              Text(
                text = "${selectedPageIndex + 1} / ${result.totalPages}",
                color = Color(0xFFE2E8F0),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )

              IconButton(
                onClick = {
                  if (selectedPageIndex < result.totalPages - 1) selectedPageIndex++
                },
                enabled = selectedPageIndex < result.totalPages - 1,
                modifier = Modifier.size(30.dp)
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                  contentDescription = "Next Page",
                  tint = if (selectedPageIndex < result.totalPages - 1) Color.White else Color(0xFF475569),
                  modifier = Modifier.size(16.dp)
                )
              }

              // Toggle single page vs all pages scroll
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (showAllPagesScroll) Color(0xFF2563EB).copy(alpha = 0.25f) else Color(0xFF1E293B))
                  .border(0.7.dp, if (showAllPagesScroll) Color(0xFF3B82F6) else Color(0xFF334155), RoundedCornerShape(6.dp))
                  .clickable { showAllPagesScroll = !showAllPagesScroll }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = if (showAllPagesScroll) (if (isKu) "هەمی بەرپەڕ" else "All Pages") else (if (isKu) "ئێک بەرپەڕ" else "Single Page"),
                  color = if (showAllPagesScroll) Color(0xFF60A5FA) else Color(0xFF94A3B8),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = if (isKu) "بەرپەڕێ ١ ژ ١ (A4 تەمام)" else "Page 1 of 1 (Standard A4)",
                color = Color(0xFF94A3B8),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }

          // Zoom Controls
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            IconButton(
              onClick = {
                zoomLevel = (zoomLevel - 0.2f).coerceAtLeast(0.7f)
              },
              enabled = zoomLevel > 0.75f,
              modifier = Modifier
                .size(30.dp)
                .testTag("print_preview_zoom_out")
            ) {
              Icon(
                imageVector = Icons.Default.ZoomOut,
                contentDescription = "Zoom Out",
                tint = if (zoomLevel > 0.75f) Color.White else Color(0xFF475569),
                modifier = Modifier.size(16.dp)
              )
            }

            Text(
              text = "${(zoomLevel * 100).toInt()}%",
              color = Color(0xFFCBD5E1),
              fontSize = 11.5.sp,
              fontWeight = FontWeight.SemiBold,
              fontFamily = FontFamily.Monospace,
              modifier = Modifier.clickable { zoomLevel = 1.0f }
            )

            IconButton(
              onClick = {
                zoomLevel = (zoomLevel + 0.2f).coerceAtMost(1.8f)
              },
              enabled = zoomLevel < 1.75f,
              modifier = Modifier
                .size(30.dp)
                .testTag("print_preview_zoom_in")
            ) {
              Icon(
                imageVector = Icons.Default.ZoomIn,
                contentDescription = "Zoom In",
                tint = if (zoomLevel < 1.75f) Color.White else Color(0xFF475569),
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }

        // -------------------------------------------------------------
        // 3. Document Canvas & A4 White-Paper Sheet Viewer
        // -------------------------------------------------------------
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .background(Color(0xFF090E17))
            .padding(12.dp),
          contentAlignment = Alignment.TopCenter
        ) {
          if (isRendering && pageBitmaps.isEmpty()) {
            Column(
              modifier = Modifier.align(Alignment.Center),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              CircularProgressIndicator(
                color = Color(0xFF10B981),
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
              )
              Text(
                text = if (isKu) "چێکرنا پێشبینینا بەرپەڕان..." else "Rendering A4 Print Preview...",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }

          val pagesToShow = if (showAllPagesScroll) (0 until result.totalPages).toList() else listOf(selectedPageIndex)

          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .testTag("print_preview_sheet_scroll"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
          ) {
            itemsIndexed(pagesToShow) { listIdx, pageIdx ->
              val sheetWidth = (350.dp * zoomLevel).coerceIn(240.dp, 680.dp)
              val sheetHeight = sheetWidth * 1.4142f // Precise A4 Ratio (1 : √2)

              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                // Page Indicator Tag above sheet
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(Color(0xFF1E293B))
                      .border(0.6.dp, Color(0xFF334155), RoundedCornerShape(4.dp))
                      .padding(horizontal = 8.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = "${if (isKu) "بەرپەڕێ" else "PAGE"} ${pageIdx + 1} / ${result.totalPages}",
                      color = Color(0xFF94A3B8),
                      fontSize = 10.5.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                }

                // Authentic A4 White-Paper Sheet
                Box(
                  modifier = Modifier
                    .width(sheetWidth)
                    .height(sheetHeight)
                    .shadow(elevation = 14.dp, shape = RoundedCornerShape(2.dp))
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White)
                    .border(0.8.dp, Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
                    .testTag("print_preview_page_${pageIdx}")
                ) {
                  val bitmap = pageBitmaps.getOrNull(pageIdx)
                  if (bitmap != null) {
                    // 1:1 Pixel-Accurate Rendered PDF Image
                    Image(
                      bitmap = bitmap.asImageBitmap(),
                      contentDescription = "PDF Page ${pageIdx + 1}",
                      modifier = Modifier.fillMaxSize()
                    )
                  } else {
                    // High-Fidelity Compose Fallback Canvas
                    A4FallbackDocumentPage(
                      result = result,
                      pageIndex = pageIdx,
                      totalPages = result.totalPages
                    )
                  }
                }
              }
            }
          }
        }

        // -------------------------------------------------------------
        // 4. Summary Strip (if present)
        // -------------------------------------------------------------
        if (result.summaryCards.isNotEmpty()) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF0F172A))
              .border(
                width = 0.8.dp,
                color = Color(0xFF1E293B)
              )
              .horizontalScroll(rememberScrollState())
              .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            result.summaryCards.forEach { card ->
              Row(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFF1E293B))
                  .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = "${card.label}:",
                  color = Color(0xFF94A3B8),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium
                )
                Text(
                  text = card.value,
                  color = Color(card.color),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.ExtraBold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }

        // -------------------------------------------------------------
        // 5. Bottom Action Bar (Trigger System Print Dialog or Share)
        // -------------------------------------------------------------
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0B132B))
            .padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Dismiss / Back Button
          OutlinedButton(
            onClick = onDismiss,
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = Color(0xFF94A3B8)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            modifier = Modifier.testTag("print_preview_dismiss_btn")
          ) {
            Text(
              text = if (isKu) "زڤڕین" else "Close",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }

          // Share PDF File Button
          OutlinedButton(
            onClick = {
              PdfExportService.sharePdfFile(context, result.file, result.documentTitle)
            },
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = Color(0xFF38BDF8)
            ),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF0284C7)),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("print_preview_share_action")
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share PDF Statement",
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isKu) "پارڤەکرنا فایلا PDF" else "Share PDF File",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }

          // Primary: Trigger System Print Dialog via PrintManager
          Button(
            onClick = {
              PdfExportService.printPdfFile(context, result.file, result.documentTitle)
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF10B981),
              contentColor = Color(0xFF041E19)
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            modifier = Modifier
              .weight(1.3f)
              .testTag("print_preview_print_action")
          ) {
            Icon(
              imageVector = Icons.Default.Print,
              contentDescription = "Trigger Print Dialog",
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isKu) "چاپکرنا فەرمی (Print)" else "Print Statement",
              fontSize = 13.5.sp,
              fontWeight = FontWeight.Black
            )
          }
        }
      }
    }
  }
}

/**
 * High-fidelity Compose layout representing an A4 page if native PdfRenderer is loading or testing.
 */
@Composable
private fun A4FallbackDocumentPage(
  result: GeneratedPdfResult,
  pageIndex: Int,
  totalPages: Int
) {
  val isKu = result.isKurdish
  val rowsPerPage = 22
  val startRow = pageIndex * rowsPerPage
  val endRow = (startRow + rowsPerPage).coerceAtMost(result.dataRows.size)
  val pageRows = if (startRow < result.dataRows.size) result.dataRows.subList(startRow, endRow) else emptyList()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    // Top Emerald Brand Line
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(3.dp)
        .background(Color(0xFF047857))
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Header Title
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = result.documentTitle,
          fontWeight = FontWeight.Black,
          fontSize = 13.sp,
          color = Color(0xFF0F172A)
        )
        Text(
          text = result.subtitle,
          fontSize = 8.5.sp,
          color = Color(0xFF475569)
        )
      }
      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "MEVAN ZEBARI PORTFOLIO",
          fontWeight = FontWeight.Bold,
          fontSize = 9.sp,
          color = Color(0xFF0F172A)
        )
        Text(
          text = result.timestampStr,
          fontSize = 8.sp,
          color = Color(0xFF64748B)
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))
    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)
    Spacer(modifier = Modifier.height(6.dp))

    // Table Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFFF1F5F9))
        .border(0.7.dp, Color(0xFF94A3B8))
        .padding(horizontal = 4.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      result.headers.take(5).forEach { headerText ->
        Text(
          text = headerText,
          fontSize = 8.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A),
          maxLines = 1
        )
      }
    }

    // Rows
    Column(modifier = Modifier.weight(1f)) {
      pageRows.forEachIndexed { rIdx, row ->
        val isEven = rIdx % 2 == 0
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(if (isEven) Color.White else Color(0xFFF8FAFC))
            .border(0.4.dp, Color(0xFFE2E8F0))
            .padding(horizontal = 4.dp, vertical = 3.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          row.cells.take(5).forEachIndexed { cIdx, cellText ->
            val isAmt = cIdx == 3 || cIdx == 4
            Text(
              text = cellText,
              fontSize = 7.5.sp,
              fontWeight = if (isAmt) FontWeight.Bold else FontWeight.Normal,
              color = if (isAmt) (if (row.isPositive) Color(0xFF047857) else Color(0xFFB91C1C)) else Color(0xFF1E293B),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }

    // Footer
    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)
    Spacer(modifier = Modifier.height(4.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = if (isKu) "بەڵگەنامەیا فەرمی • هەمی ماف پاراستینە © Mevan Zebari" else "Confidential Financial Statement • All rights reserved",
        fontSize = 7.sp,
        color = Color(0xFF94A3B8)
      )
      Text(
        text = "Page ${pageIndex + 1} of $totalPages",
        fontSize = 7.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF64748B)
      )
    }
  }
}
