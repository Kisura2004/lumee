package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Greeting
import com.example.data.GreetingProvider
import com.example.ui.LumeeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesScreen(viewModel: LumeeViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Greeting.Category?>(null) }
    
    // Asynchronous search results to maintain buttery-smooth 60+ FPS scrolling
    val coroutineScope = rememberCoroutineScope()
    val filteredQuotes = remember { mutableStateListOf<Greeting>() }
    var isSearching by remember { mutableStateOf(false) }

    // Synchronize or compute list when query or filter changes
    LaunchedEffect(searchQuery, selectedCategory) {
        isSearching = true
        coroutineScope.launch(Dispatchers.Default) {
            val q = searchQuery.trim().lowercase(Locale.getDefault())
            
            // If the query is an integer or starts with # followed by integer, let them lookup specific index directly
            val indexLookup = if (q.startsWith("#")) {
                q.drop(1).toIntOrNull()
            } else {
                q.toIntOrNull()
            }

            val results = if (indexLookup != null && indexLookup in 0 until GreetingProvider.TOTAL_GENERATIVE_QUOTES) {
                listOf(GreetingProvider.getQuoteAt(indexLookup))
            } else if (q.isEmpty()) {
                // If query is empty, show a sample of first 400 quotes of selected category to keep memory tiny
                val list = mutableListOf<Greeting>()
                var matches = 0
                for (i in 0 until GreetingProvider.TOTAL_GENERATIVE_QUOTES) {
                    val g = GreetingProvider.getQuoteAt(i)
                    if (selectedCategory == null || g.category == selectedCategory) {
                        list.add(g)
                        matches++
                    }
                    if (matches >= 400) break
                }
                list
            } else {
                // Search keyword across all 40,000 quotes
                val list = mutableListOf<Greeting>()
                var limit = 400
                for (i in 0 until GreetingProvider.TOTAL_GENERATIVE_QUOTES) {
                    val g = GreetingProvider.getQuoteAt(i)
                    val matchCategory = selectedCategory == null || g.category == selectedCategory
                    val matchQuery = g.text.lowercase(Locale.getDefault()).contains(q) || 
                                     (g.author ?: "").lowercase(Locale.getDefault()).contains(q)
                    
                    if (matchCategory && matchQuery) {
                        list.add(g)
                        limit--
                        if (limit <= 0) break
                    }
                }
                list
            }

            launch(Dispatchers.Main) {
                filteredQuotes.clear()
                filteredQuotes.addAll(results)
                isSearching = false
            }
        }
    }

    var contemplationQuote by remember { mutableStateOf<Greeting?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("quotes_screen_root"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Screen Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Quote Sanctuary",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color(0xFF3D3834)
            )
            Text(
                text = "Seek guidance from a world of ${GreetingProvider.TOTAL_GENERATIVE_QUOTES} paths of light.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF3D3834).copy(alpha = 0.6f)
            )
        }

        // Luxurious Rounded Glass Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quote_search_field"),
            placeholder = { Text("Search 40,000 quotes or enter #12345...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = Color(0xFF3D3834).copy(alpha = 0.5f)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = Color(0xFF3D3834).copy(alpha = 0.5f)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.4f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.25f),
                focusedBorderColor = Color(0xFFE0A7A7),
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedTextColor = Color(0xFF3D3834),
                unfocusedTextColor = Color(0xFF3D3834)
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            )
        )

        // Horizontal Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val categories = listOf(
                null to "All",
                Greeting.Category.GENTLE_GROUNDING to "Grounding",
                Greeting.Category.HAPPY_UPLIFTING to "Uplifting",
                Greeting.Category.REFLECTIVE_THOUGHTFUL to "Reflective"
            )

            categories.forEach { (cat, label) ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Color(0xFFE0A7A7).copy(alpha = 0.25f)
                            else Color.White.copy(alpha = 0.2f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFFE0A7A7) else Color.White.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF8B5A5A) else Color(0xFF3D3834)
                    )
                }
            }
        }

        // Matching list count / status helper
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isSearching) "Gathering insights..." else "Showing ${filteredQuotes.size} discoveries",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3D3834).copy(alpha = 0.5f)
            )

            if (searchQuery.isEmpty() && selectedCategory == null) {
                Text(
                    text = "Browsing first 400; search for custom patterns!",
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFF3D3834).copy(alpha = 0.4f)
                )
            }
        }

        // Main Lazy Column
        if (filteredQuotes.isEmpty() && !isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = "No quotes found",
                        tint = Color(0xFF3D3834).copy(alpha = 0.3f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "The universe remains quiet. Try another search.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF3D3834).copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            val listState = rememberLazyListState()
            
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("quotes_lazy_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(
                    items = filteredQuotes,
                    key = { it.id }
                ) { quote ->
                    QuoteCard(
                        quote = quote,
                        index = quote.id - 10000,
                        onClick = { contemplationQuote = quote }
                    )
                }
            }
        }
    }

    // contemplation dialog
    contemplationQuote?.let { quote ->
        Dialog(onDismissRequest = { contemplationQuote = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFFFFF9F5))
                    .border(
                        width = 1.6.dp,
                        color = Color(0xFFE0A7A7),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(26.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "✦ Contemplation #${quote.id - 10000} ✦",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE0A7A7),
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = "Category: ${quote.category.name.replace("_", " ")}",
                        fontSize = 11.sp,
                        color = Color(0xFF3D3834).copy(alpha = 0.5f),
                        fontWeight = FontWeight.SemiBold
                    )

                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(1.dp)
                            .background(Color(0xFFE0A7A7).copy(alpha = 0.4f))
                    )

                    Text(
                        text = "“${quote.text}”",
                        style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
                        color = Color(0xFF3D3834),
                        lineHeight = 32.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Text(
                        text = "— ${quote.author ?: "Unknown Sage"}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF8B5A5A)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { contemplationQuote = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE0A7A7)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Return to silence",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuoteCard(
    quote: Greeting,
    index: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.3f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#$index",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE0A7A7)
                )

                Text(
                    text = quote.category.name.replace("_", " ").lowercase(Locale.getDefault()),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3D3834).copy(alpha = 0.4f),
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Text(
                text = "“${quote.text}”",
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                color = Color(0xFF3D3834),
                lineHeight = 22.sp
            )

            Text(
                text = "— ${quote.author ?: "Unknown Sage"}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF8B5A5A),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
