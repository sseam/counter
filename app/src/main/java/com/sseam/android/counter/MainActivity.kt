package com.sseam.android.counter

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.sseam.android.counter.config.Constants.Companion.BANNER_AD_UNIT_ID
import com.sseam.android.counter.config.Constants.Companion.BANNER_AD_UNIT_ID_TEST
import com.sseam.android.counter.ui.theme.CounterTheme
import com.sseam.android.counter.ui.theme.RetroColor1
import com.sseam.android.counter.ui.theme.RetroColor11

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CounterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Counter()
                }
            }
        }

        MobileAds.initialize(this) { }

    }
}

@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    val countHistory = remember { mutableStateListOf<Int>() }
    val prefs = LocalContext.current.getSharedPreferences("main_pref", Context.MODE_PRIVATE)
    //val prefs = PreferenceManager.getDefaultSharedPreferences(LocalContext.current)
    val editor = prefs.edit()
    // Load count history from SharedPreferences
    for (i in 0 until MAX_HISTORY_COUNT) {
        val countValue = prefs.getInt("count$i", -1)
        if (countValue != -1) {
            countHistory.add(countValue)
        }
    }
    ConstraintLayout(modifier = Modifier
        .fillMaxSize()
        .padding(bottom = 0.dp)) {
        val (banner, row, column) = createRefs()

        Row(
            modifier = Modifier
                .height(100.dp)
                .constrainAs(banner) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            BannerAdView()
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 8.dp, end = 8.dp)
                .constrainAs(row) {
                    top.linkTo(banner.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { count = 0 },
                modifier = Modifier
                    .size(50.dp)
            ) {
                Icon(
                    Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp)
                )
            }
            IconButton(
                onClick = {
                    countHistory.add(count)
                    if (countHistory.size > MAX_HISTORY_COUNT) {
                        countHistory.removeFirst()
                    }
                    for (i in 0 until countHistory.size) {
                        editor.putInt("count$i", countHistory[i])
                    }
                    editor.apply()
                },
                modifier = Modifier
                    .size(50.dp)
            ) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                )
            }
        }
        //저장 기록 표시
        Column(
            modifier = Modifier
                .fillMaxSize()
                .constrainAs(column) {
                    top.linkTo(row.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val showDialog = remember { mutableStateOf(false) }
            Spacer(modifier = Modifier.height(80.dp))
            Text(
                text = "${stringResource(R.string.title_history)} ${countHistory.joinToString()}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {  },
                            onDoubleTap = { /* Called on Double Tap */ },
                            onLongPress = { showDialog.value = true },
                            onTap = { /* Called on Tap */ }
                        )
                    }
            )
            if (showDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDialog.value = false },
                    title = { Text(text = stringResource(R.string.app_name)) },
                    text = { Text(text = stringResource(id = R.string.msg_reset)) },
                    confirmButton = {
                        Button(
                            onClick = {
                                // 초기화 작업 수행
                                countHistory.clear()
                                showDialog.value = false
                            }
                        ) {
                            Text(text = stringResource(id = R.string.msg_confirm))
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                showDialog.value = false
                            }
                        ) {
                            Text(text = stringResource(id = R.string.msg_confirm))
                        }
                    },
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    )
                )
            }
        }
        //중앙 카운트
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = count.toString(),
                fontSize = 84.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        //하단 버튼
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 0.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    if(count > 0) count--
                          },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .align(Alignment.BottomStart)
                    .height(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RetroColor11,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(0.dp)
            ) {
                Text(stringResource(id = R.string.msg_decrease), fontSize = 20.sp)
            }
            Button(
                onClick = { count++ },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .align(Alignment.BottomEnd)
                    .height(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RetroColor1,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(0.dp)
            ) {
                Text(stringResource(id = R.string.msg_increase), fontSize = 20.sp)
            }
        }
    }
}
const val MAX_HISTORY_COUNT = 5

@Composable
fun BannerAdView(
    isTest: Boolean = false
) {
    val unitId = if (isTest) BANNER_AD_UNIT_ID_TEST else BANNER_AD_UNIT_ID

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.LARGE_BANNER)
                adUnitId = unitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
fun CounterPreview() {
    CounterTheme {
        Counter()
    }
}