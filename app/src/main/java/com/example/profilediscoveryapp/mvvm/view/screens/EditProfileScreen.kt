package com.example.profilediscoveryapp.mvvm.view.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.profilediscoveryapp.mvvm.model.dataclass.Profile
import com.example.profilediscoveryapp.mvvm.viewmodel.ProfileViewModel
import com.example.profilediscoveryapp.mvvm.view.utils.components.LoadingSpinner
import com.google.accompanist.permissions.*
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val updateSuccess by viewModel.updateSuccess.collectAsStateWithLifecycle()
    val imageUploadProgress by viewModel.imageUploadProgress.collectAsStateWithLifecycle()

    // State for showing image picker dialog
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isImageUploading by remember { mutableStateOf(false) }

    // Form states - initialize with profile data
    var fullName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var aboutMe by remember { mutableStateOf("") }

    // Error states - only for button click validation
    var showErrors by remember { mutableStateOf(false) }
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var ageError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    // Permissions
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val galleryPermissionState = rememberPermissionState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    )

    // Image picker launcher for gallery
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            isImageUploading = true
            val file = uriToFile(context, it)
            viewModel.updateProfilePicture(file)
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            isImageUploading = true
            val file = bitmapToFile(context, it)
            selectedImageUri = Uri.fromFile(file)
            viewModel.updateProfilePicture(file)
        }
    }

    // Load profile data into form
    LaunchedEffect(profile) {
        if (profile != null) {
            fullName = profile!!.fullName
            age = profile!!.age.toString()
            email = profile!!.email
            phoneNumber = profile!!.phoneNumber
            occupation = profile!!.occupation
            location = profile!!.location
            aboutMe = profile!!.aboutMe
            Log.d("EditProfileScreen", "Loaded existing profile")
        }
    }

    // Handle save success
    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            delay(500)
            viewModel.clearUpdateSuccess()
            onSaveSuccess()
        }
    }

    // Reset image uploading state after progress completes
    LaunchedEffect(imageUploadProgress) {
        if (imageUploadProgress == 0 && isImageUploading) {
            isImageUploading = false
        }
    }

    // Validation function
    fun validateAndSave() {
        var isValid = true

        if (fullName.isEmpty()) {
            fullNameError = "Full name is required"
            isValid = false
        } else {
            fullNameError = null
        }

        if (age.isEmpty()) {
            ageError = "Age is required"
            isValid = false
        } else if (age.toIntOrNull() == null) {
            ageError = "Please enter a valid age"
            isValid = false
        } else {
            ageError = null
        }

        if (email.isEmpty()) {
            emailError = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Please enter a valid email"
            isValid = false
        } else {
            emailError = null
        }

        if (phoneNumber.isEmpty()) {
            phoneError = "Phone number is required"
            isValid = false
        } else {
            phoneError = null
        }

        showErrors = true

        if (isValid) {
            val ageInt = age.toIntOrNull() ?: 0
            val userId = profile?.userId ?: UUID.randomUUID().toString()
            val existingId = profile?.id ?: ""
            val cloudinaryUrl = viewModel.getUploadedImageUrl()
            val finalImageUrl = cloudinaryUrl ?: profile?.profilePictureUrl

            val updatedProfile = Profile(
                id = existingId,
                userId = userId,
                fullName = fullName,
                age = ageInt,
                email = email,
                phoneNumber = phoneNumber,
                occupation = occupation,
                location = location,
                aboutMe = aboutMe,
                profilePictureUrl = finalImageUrl
            )

            if (profile == null) {
                viewModel.createProfile(updatedProfile)
            } else {
                viewModel.updateProfile(updatedProfile)
            }
            viewModel.clearUploadedImageUrl()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (profile == null) "Create Profile" else "Edit Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onSaveSuccess) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Picture Section with Camera Button Below
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Profile Image
                    Card(
                        modifier = Modifier
                            .size(130.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val displayImage = when {
                                selectedImageUri != null -> selectedImageUri
                                profile?.profilePictureUrl != null -> profile?.profilePictureUrl
                                else -> "https://randomuser.me/api/portraits/men/1.jpg"
                            }

                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(displayImage)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Change Photo Button (Outside the image)
                    OutlinedButton(
                        onClick = { showImagePickerDialog = true },
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(36.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Change Photo",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Change Photo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Image upload progress
                if (isImageUploading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(0.7f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LinearProgressIndicator(
                                progress = imageUploadProgress / 100f,
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Uploading... $imageUploadProgress%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Form Fields Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Full Name
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name", fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            isError = showErrors && fullNameError != null,
                            supportingText = {
                                if (showErrors && fullNameError != null) {
                                    Text(fullNameError!!, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        // Age
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("Age", fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Cake,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = showErrors && ageError != null,
                            supportingText = {
                                if (showErrors && ageError != null) {
                                    Text(ageError!!, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email", fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            isError = showErrors && emailError != null,
                            supportingText = {
                                if (showErrors && emailError != null) {
                                    Text(emailError!!, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        // Phone Number
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number", fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            isError = showErrors && phoneError != null,
                            supportingText = {
                                if (showErrors && phoneError != null) {
                                    Text(phoneError!!, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        // Occupation
                        OutlinedTextField(
                            value = occupation,
                            onValueChange = { occupation = it },
                            label = { Text("Occupation", fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Work,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        // Location
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location", fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        // About Me
                        OutlinedTextField(
                            value = aboutMe,
                            onValueChange = { aboutMe = it },
                            label = { Text("About Me", fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Save Button
                Button(
                    onClick = { validateAndSave() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading && !isImageUploading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    when {
                        isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Saving...", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        isImageUploading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                                progress = imageUploadProgress / 100f
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Uploading Image...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        else -> {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            if (isLoading && !isImageUploading) {
                LoadingSpinner()
            }
        }
    }

    // Image Picker Dialog
    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            title = {
                Text(
                    "Choose Profile Picture",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = { Text("Select from camera or gallery to update your profile picture") },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = {
                            showImagePickerDialog = false
                            cameraPermissionState.launchPermissionRequest()
                            if (cameraPermissionState.status.isGranted) {
                                cameraLauncher.launch(null)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Camera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Camera", fontWeight = FontWeight.Medium)
                    }
                    TextButton(
                        onClick = {
                            showImagePickerDialog = false
                            galleryPermissionState.launchPermissionRequest()
                            if (galleryPermissionState.status.isGranted) {
                                imagePickerLauncher.launch("image/*")
                            }
                        }
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gallery", fontWeight = FontWeight.Medium)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showImagePickerDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// Helper functions
fun uriToFile(context: Context, uri: Uri): File {
    try {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: return File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
        val tempFile = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        inputStream.close()
        return tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        return File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
    }
}

fun bitmapToFile(context: Context, bitmap: Bitmap): File {
    try {
        val tempFile = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
        FileOutputStream(tempFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        }
        return tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        return File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
    }
}