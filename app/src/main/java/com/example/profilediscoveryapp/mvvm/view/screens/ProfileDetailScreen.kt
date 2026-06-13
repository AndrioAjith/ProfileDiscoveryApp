package com.example.profilediscoveryapp.mvvm.view.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.profilediscoveryapp.mvvm.viewmodel.ProfileDetailViewModel
import com.example.profilediscoveryapp.mvvm.view.utils.components.LoadingSpinner
import com.google.accompanist.permissions.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ProfileDetailScreen(
    viewModel: ProfileDetailViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val isSaved by viewModel.isSaved.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val updateSuccess by viewModel.updateSuccess.collectAsStateWithLifecycle()
    val imageUploadProgress by viewModel.imageUploadProgress.collectAsStateWithLifecycle()

    var showEditDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isImageUploading by remember { mutableStateOf(false) }

    var editFullName by remember { mutableStateOf("") }
    var editAge by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editPhoneNumber by remember { mutableStateOf("") }
    var editOccupation by remember { mutableStateOf("") }
    var editLocation by remember { mutableStateOf("") }
    var editAboutMe by remember { mutableStateOf("") }

    var fullNameError by remember { mutableStateOf<String?>(null) }
    var ageError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val galleryPermissionState = rememberPermissionState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            isImageUploading = true
            val file = uriToFileHelper(context, it)
            viewModel.updateProfilePicture(file)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            isImageUploading = true
            val file = bitmapToFileHelper(context, it)
            selectedImageUri = Uri.fromFile(file)
            viewModel.updateProfilePicture(file)
        }
    }

    LaunchedEffect(showEditDialog, profile) {
        if (showEditDialog && profile != null) {
            editFullName = profile!!.fullName
            editAge = profile!!.age.toString()
            editEmail = profile!!.email
            editPhoneNumber = profile!!.phoneNumber
            editOccupation = profile!!.occupation
            editLocation = profile!!.location
            editAboutMe = profile!!.aboutMe
            selectedImageUri = null
        }
    }

    LaunchedEffect(imageUploadProgress) {
        if (imageUploadProgress == 0 && isImageUploading) {
            isImageUploading = false
        }
    }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            showEditDialog = false
            viewModel.clearUpdateSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                    IconButton(onClick = { viewModel.toggleSaveProfile() }) {
                        Icon(
                            if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isSaved) "Saved" else "Save"
                        )
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
            if (isLoading) {
                LoadingSpinner()
            } else if (profile != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Image Section
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .shadow(12.dp, CircleShape)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(
                                        profile?.profilePictureUrl
                                            ?: "https://randomuser.me/api/portraits/men/1.jpg"
                                    )
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Name and Title
                    Text(
                        text = profile?.fullName ?: "",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Work,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = profile?.occupation ?: "",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = profile?.location ?: "",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCard(
                            value = "${profile?.age ?: 0}",
                            label = "Years",
                            icon = "🎂"
                        )
                        StatCard(
                            value = profile?.occupation?.split(" ")?.firstOrNull()?.take(10)
                                ?: "N/A",
                            label = "Role",
                            icon = "💼"
                        )
                        StatCard(
                            value = if (isSaved) "Saved" else "Active",
                            label = "Status",
                            icon = if (isSaved) "⭐" else "🟢"
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // About Me Section
                    if (!profile?.aboutMe.isNullOrEmpty()) {
                        InfoCardItem(
                            title = "About Me",
                            content = profile?.aboutMe ?: "",
                            icon = "📝"
                        )
                    }

                    // Contact Information Section
                    InfoCardItem(
                        title = "Contact Information",
                        content = buildString {
                            append("📧 Email: ${profile?.email ?: "Not provided"}\n")
                            append("📱 Phone: ${profile?.phoneNumber ?: "Not provided"}\n")
                            append("📍 Location: ${profile?.location ?: "Not provided"}")
                        },
                        icon = "📞"
                    )
                }
            }

            if (showEditDialog) {
                EditProfileDialogWithImage(
                    fullName = editFullName,
                    onFullNameChange = { editFullName = it },
                    age = editAge,
                    onAgeChange = { editAge = it },
                    email = editEmail,
                    onEmailChange = { editEmail = it },
                    phoneNumber = editPhoneNumber,
                    onPhoneNumberChange = { editPhoneNumber = it },
                    occupation = editOccupation,
                    onOccupationChange = { editOccupation = it },
                    location = editLocation,
                    onLocationChange = { editLocation = it },
                    aboutMe = editAboutMe,
                    onAboutMeChange = { editAboutMe = it },
                    profilePictureUrl = profile?.profilePictureUrl,
                    selectedImageUri = selectedImageUri,
                    isImageUploading = isImageUploading,
                    imageUploadProgress = imageUploadProgress,
                    fullNameError = fullNameError,
                    ageError = ageError,
                    emailError = emailError,
                    phoneError = phoneError,
                    onDismiss = { showEditDialog = false },
                    onSave = {
                        var isValid = true

                        if (editFullName.isEmpty()) {
                            fullNameError = "Full name is required"
                            isValid = false
                        } else {
                            fullNameError = null
                        }

                        if (editAge.isEmpty()) {
                            ageError = "Age is required"
                            isValid = false
                        } else if (editAge.toIntOrNull() == null) {
                            ageError = "Please enter a valid age"
                            isValid = false
                        } else {
                            ageError = null
                        }

                        if (editEmail.isEmpty()) {
                            emailError = "Email is required"
                            isValid = false
                        } else if (!Patterns.EMAIL_ADDRESS.matcher(editEmail).matches()) {
                            emailError = "Please enter a valid email"
                            isValid = false
                        } else {
                            emailError = null
                        }

                        if (editPhoneNumber.isEmpty()) {
                            phoneError = "Phone number is required"
                            isValid = false
                        } else {
                            phoneError = null
                        }

                        if (isValid) {
                            val updatedProfile = profile?.copy(
                                fullName = editFullName,
                                age = editAge.toIntOrNull() ?: 0,
                                email = editEmail,
                                phoneNumber = editPhoneNumber,
                                occupation = editOccupation,
                                location = editLocation,
                                aboutMe = editAboutMe
                            )
                            updatedProfile?.let {
                                viewModel.updateProfile(it)
                            }
                        }
                    },
                    onCameraClick = {
                        cameraPermissionState.launchPermissionRequest()
                        if (cameraPermissionState.status.isGranted) {
                            cameraLauncher.launch(null)
                        }
                    },
                    onGalleryClick = {
                        galleryPermissionState.launchPermissionRequest()
                        if (galleryPermissionState.status.isGranted) {
                            imagePickerLauncher.launch("image/*")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun StatCard(value: String, label: String, icon: String) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EditProfileDialogWithImage(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    age: String,
    onAgeChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    occupation: String,
    onOccupationChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    aboutMe: String,
    onAboutMeChange: (String) -> Unit,
    profilePictureUrl: String?,
    selectedImageUri: Uri?,
    isImageUploading: Boolean,
    imageUploadProgress: Int,
    fullNameError: String?,
    ageError: String?,
    emailError: String?,
    phoneError: String?,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    val context = LocalContext.current
    var showImageSourceDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Edit Profile",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Update your personal information",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Picture Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val displayImage = when {
                                selectedImageUri != null -> selectedImageUri
                                !profilePictureUrl.isNullOrEmpty() -> profilePictureUrl
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

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showImageSourceDialog = true },
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Change Photo", fontSize = 12.sp)
                    }
                }

                if (isImageUploading) {
                    LinearProgressIndicator(
                        progress = imageUploadProgress / 100f,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Uploading... $imageUploadProgress%",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                OutlinedTextField(
                    value = fullName,
                    onValueChange = onFullNameChange,
                    label = { Text("Full Name") },
                    isError = fullNameError != null,
                    supportingText = {
                        fullNameError?.let {
                            Text(
                                it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = age,
                    onValueChange = onAgeChange,
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = ageError != null,
                    supportingText = {
                        ageError?.let {
                            Text(
                                it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = emailError != null,
                    supportingText = {
                        emailError?.let {
                            Text(
                                it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = onPhoneNumberChange,
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = phoneError != null,
                    supportingText = {
                        phoneError?.let {
                            Text(
                                it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = occupation,
                    onValueChange = onOccupationChange,
                    label = { Text("Occupation") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = onLocationChange,
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = aboutMe,
                    onValueChange = onAboutMeChange,
                    label = { Text("About Me") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !isImageUploading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isImageUploading) "Uploading..." else "Save Changes",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Cancel")
            }
        }
    )

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Choose Profile Picture", fontWeight = FontWeight.Bold) },
            text = { Text("Select from camera or gallery") },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = { showImageSourceDialog = false; onCameraClick() }
                    ) {
                        Icon(Icons.Default.Camera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Camera")
                    }
                    TextButton(
                        onClick = { showImageSourceDialog = false; onGalleryClick() }
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gallery")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showImageSourceDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun InfoCardItem(title: String, content: String, icon: String = "📋") {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = icon, fontSize = 20.sp)
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Helper functions
private fun uriToFileHelper(context: Context, uri: Uri): File {
    val inputStream: InputStream = context.contentResolver.openInputStream(uri)
        ?: return File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
    val tempFile = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
    FileOutputStream(tempFile).use { outputStream ->
        inputStream.copyTo(outputStream)
    }
    inputStream.close()
    return tempFile
}

private fun bitmapToFileHelper(context: Context, bitmap: Bitmap): File {
    val tempFile = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
    FileOutputStream(tempFile).use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
    }
    return tempFile
}