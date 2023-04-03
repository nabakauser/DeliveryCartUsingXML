package com.example.deliverycartusingxml.home

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.contains
import com.example.deliverycartusingxml.BuildConfig
import com.example.deliverycartusingxml.Constants.EXTRA_REQUEST_CODE
import com.example.deliverycartusingxml.Constants.KEY_ADDRESS
import com.example.deliverycartusingxml.R
import com.example.deliverycartusingxml.databinding.ActivityMainBinding
import com.example.deliverycartusingxml.maps.MapActivity
import com.example.deliverycartusingxml.maps.MapsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class MainActivity : AppCompatActivity() {
    private val cartViewModel: CartViewModel by viewModel()

    //private var purchaseCost: String? = null
    private var setImageUri: Uri? = null
    private val resultContracts by lazy {
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            handleActivityResults(activityResult)
        }
    }
    private val galleryResultContracts by lazy {
        registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
            if (imageUri != null) {
                addImage(imageUri)
            }
        }
    }
    private val cameraResultContracts by lazy {
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                setImageUri?.let { imageUri ->
                    addImage(imageUri)
                }
            }
        }
    }


    private var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        resultContracts
        galleryResultContracts
        cameraResultContracts
        setContentView(binding?.root)
        setUpListeners()
        // Log.e("xxxxxx", "onCreate: $purchaseCost", )
    }

    private fun handleActivityResults(activityResult: ActivityResult?) {
        val result = activityResult?.data?.getStringExtra(KEY_ADDRESS)
        if (activityResult != null) {
            if (activityResult.resultCode == 100) {
                binding?.uiEtPickUpAddress?.text = result
            } else if (activityResult.resultCode == 200) {
                binding?.uiEtDeliveryAddress?.text = result
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun setUpListeners() {
        binding?.uiBtnChangePA?.setOnClickListener {
            navigateToMapsActivity("PICK_UP_KEY")
        }
        binding?.uiBtnChangeDA?.setOnClickListener {
            navigateToMapsActivity("DELIVERY_KEY")
        }
        binding?.uiIvBtnAddImages?.setOnClickListener {
            setUpAddImageBottomSheet()
        }
        binding?.uiIvPurchaseDropDown?.setOnClickListener {
            setUpPurchaseCostBottomSheet()
        }
        cartViewModel.purchaseCost.observe(this) {
            binding?.uiTvEnterCost?.text = "$it INR"
        }
        //Log.e("purrrc", "setUpListeners: $purchaseCost", )
        binding?.uiIvPaymentType?.setOnClickListener {
            setUpPaymentMethodBottomSheet()
        }
        cartViewModel.paymentMethod.observe(this) { paymentMethod ->
            binding?.uiTvEnterPayMethod?.text = paymentMethod
            Log.e("youBetterWorkk", "setUpListeners: $paymentMethod") // you diddddd!!! saranghaeeee jhagiyaaaa \^0^/
        }
    }

    private fun navigateToMapsActivity(requestCode: String) {
        val intent = Intent(this, MapActivity::class.java)
        intent.putExtra(EXTRA_REQUEST_CODE, requestCode)
        resultContracts.launch(intent)
    }

    @SuppressLint("InflateParams", "MissingInflatedId")
    private fun setUpAddImageBottomSheet() {

        val addImageBottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.get_image_layout, null)
        val uiTvGetImageFromGallery = view.findViewById<TextView>(R.id.uiTvGetImageFromGallery)
        val uiTvGetImageFromCamera = view.findViewById<TextView>(R.id.uiTvGetImageFromCamera)

        uiTvGetImageFromGallery.setOnClickListener {
            galleryResultContracts.launch("image/*")
            addImageBottomSheet.dismiss()
        }

        uiTvGetImageFromCamera.setOnClickListener {
            getTmpFileUri().let { uri ->
                setImageUri = uri
                cameraResultContracts.launch(setImageUri)
                addImageBottomSheet.dismiss()
            }
        }

        addImageBottomSheet.setCancelable(true)
        addImageBottomSheet.setContentView(view)
        addImageBottomSheet.show()
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(
            applicationContext,
            "${BuildConfig.APPLICATION_ID}.provider",
            tmpFile
        )
    }

    private fun addImage(image: Uri) {
        binding?.uiImage?.setImageURI(image)
    }

    @SuppressLint("MissingInflatedId")
    private fun setUpPurchaseCostBottomSheet() {
        val purchaseCostBottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.get_purchase_cost_layout, null)
        val uiEtPurchaseCost = view.findViewById<EditText>(R.id.uiEtPurchaseCost)
        val uiBtnDone = view.findViewById<Button>(R.id.uiBtnDone)

        uiBtnDone.setOnClickListener {
            cartViewModel.purchaseCost.value = uiEtPurchaseCost.text.toString()
            //Log.e("purrrc", "setUpListeners: $purchaseCost", )
            purchaseCostBottomSheet.dismiss()
        }
        purchaseCostBottomSheet.setCancelable(true)
        purchaseCostBottomSheet.setContentView(view)
        purchaseCostBottomSheet.show()
    }

    @SuppressLint("MissingInflatedId", "InflateParams")
    private fun setUpPaymentMethodBottomSheet() {
        val paymentMethodBottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.payment_method_layout, null)
        val radioGroup = view.findViewById<RadioGroup>(R.id.uiRgPaymentMethod)
        val uiBtnDone = view.findViewById<Button>(R.id.uiBtnDone)

        radioGroup.setOnCheckedChangeListener { rGroup, selectedItem ->
            Log.d("RadioButton", "rgroup: ${rGroup}") //check two // got same id as the next line log // dont know why i used this tho
            Log.d("RadioButton", "RadioGroup: ${radioGroup}")  //check one //got correct id
            val selectedRadio = view.findViewById<RadioButton>(selectedItem) // <-check 4 & check 5 ->(radioGroup.checkedRadioButtonId) // this works too
            val text = selectedRadio.text //lucky 13 alwaysss worksss ^u^ ^u^ ^u^
            Log.d("RadioButton", "selectedRadio: ${text}") // check 13 (please workkk!!) FUCKINGGGG FINALLYYYY // just get updated noww T_T T_T //it diddd T_T // after 2 fucking hours (0-0)
            Log.d("RadioButton", "selectedRadio: ${selectedRadio}") //check 4&5 //tried to get id // got some wierdass id XD XD // but text wont update yet T_T
            Log.d("RadioButton", "selectedRadioUI: ${R.id.uiRbTypeCard}")

//            if (R.id.uiRbTypeCard == selectedItem) {
//                Log.d("RadioButton", "Selected RadioButtonCheck: credit") // check three // doesnt work T_T // waiiittt it doesss workk \^0^/
//                //cartViewModel.paymentMethod.value = selectedRadioButton.text.toString()
//            } else {
//                Log.e("RadioButton", "Selected RadioButton is null or not part of the radioGroup ${selectedItem}")
//            }
            cartViewModel.paymentMethod.value = text.toString()
        }
        uiBtnDone.setOnClickListener {
            // radio button text doesnt work inside this onCLick //ask tmrw //kjhkjhkhhoshitdoesntwork!!!! //onClick doesnot work inside an Onlick you dumbfuck!! #i4ma
            paymentMethodBottomSheet.dismiss()
        }
        paymentMethodBottomSheet.setCancelable(true)
        paymentMethodBottomSheet.setContentView(view)
        paymentMethodBottomSheet.show()
    }
}