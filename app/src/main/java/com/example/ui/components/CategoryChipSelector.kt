package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.categories.AppCategoryRegistry
import com.example.ui.categories.CategorySectionType
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryChipSelector(
  section: CategorySectionType,
  selectedCategory: String,
  onCategorySelected: (String) -> Unit,
  customCategories: List<String> = emptyList(),
  onAddCustomCategory: ((String) -> Unit)? = null,
  isKurdish: Boolean = true,
  modifier: Modifier = Modifier
) {
  var showAddDialog by remember { mutableStateOf(false) }
  var newCategoryText by remember { mutableStateOf("") }

  val allCategories = remember(section, customCategories, isKurdish) {
    AppCategoryRegistry.getCombinedCategoryNames(section, customCategories, isKurdish)
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (isKurdish) "📂 هەلبژارتنا پۆلێ (ب ئێک دەستلێدان):" else "📂 Select Category (1-Tap):",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      if (selectedCategory.isNotBlank()) {
        Text(
          text = if (isKurdish) "دیارکری: $selectedCategory" else "Selected: $selectedCategory",
          fontSize = 11.sp,
          fontWeight = FontWeight.ExtraBold,
          color = AppCategoryRegistry.findCategoryColor(selectedCategory)
        )
      }
    }

    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      allCategories.forEach { cat ->
        val isSelected = selectedCategory.equals(cat, ignoreCase = true) ||
          (selectedCategory.isNotBlank() && cat.contains(selectedCategory, ignoreCase = true)) ||
          (selectedCategory.isNotBlank() && selectedCategory.contains(cat, ignoreCase = true))
        val catColor = AppCategoryRegistry.findCategoryColor(cat)

        Box(
          modifier = Modifier
            .heightIn(min = 36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
              if (isSelected) catColor.copy(alpha = 0.22f)
              else Color(0xFF1E293B).copy(alpha = 0.6f)
            )
            .border(
              width = if (isSelected) 1.5.dp else 0.8.dp,
              color = if (isSelected) catColor else catColor.copy(alpha = 0.35f),
              shape = RoundedCornerShape(10.dp)
            )
            .clickable { onCategorySelected(cat) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("category_chip_${cat.replace(" ", "_")}"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            if (isSelected) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = catColor,
                modifier = Modifier.size(14.dp)
              )
            }
            Text(
              text = cat,
              fontSize = 11.5.sp,
              fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
              color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
            )
          }
        }
      }

      // "+ پۆلا نوو" Button
      if (onAddCustomCategory != null) {
        Box(
          modifier = Modifier
            .heightIn(min = 36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(GoldAccent.copy(alpha = 0.15f))
            .border(1.dp, GoldAccent.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .clickable {
              newCategoryText = ""
              showAddDialog = true
            }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("add_custom_category_chip_btn"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = null,
              tint = GoldAccent,
              modifier = Modifier.size(15.dp)
            )
            Text(
              text = if (isKurdish) "+ پۆلا نوو" else "+ New Category",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = GoldAccent
            )
          }
        }
      }
    }
  }

  // Quick Add Custom Category Dialog
  if (showAddDialog) {
    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = {
        Text(
          text = if (isKurdish) "✨ زێدەکرنا پۆلا نوو" else "✨ Add New Category",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = if (isKurdish) "ناڤێ پۆلێ یان جۆرێ دلخواز بنڤیسە:" else "Enter the name for your custom category:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          OutlinedTextField(
            value = newCategoryText,
            onValueChange = { newCategoryText = it },
            placeholder = {
              Text(
                text = if (isKurdish) "بۆ نموونە: گەشت، باخچە، هتد..." else "e.g., Travel, Garden...",
                fontSize = 12.sp
              )
            },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("new_custom_category_name_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val clean = newCategoryText.trim()
            if (clean.isNotBlank()) {
              onAddCustomCategory?.invoke(clean)
              onCategorySelected(clean)
              showAddDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight, contentColor = Color(0xFF041E19))
        ) {
          Text(text = if (isKurdish) "پاشەکەوتکرن" else "Save", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showAddDialog = false }) {
          Text(text = if (isKurdish) "پاشگەزبوون" else "Cancel")
        }
      }
    )
  }
}
