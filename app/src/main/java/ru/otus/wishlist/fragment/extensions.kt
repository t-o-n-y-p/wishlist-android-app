package ru.otus.wishlist.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.otus.wishlist.R

fun BottomSheetDialogFragment.dismissWithToast(resId: Int) {
    dismiss()
    Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
}

fun Fragment.setFragmentResult(fragment: String, key: String, value: String) =
    setFragmentResult(
        fragment,
        Bundle().apply { putString(key, value) }
    )

fun Context.showErrorAlert(): AlertDialog =
    MaterialAlertDialogBuilder(this, R.style.Alert)
        .setTitle(getString(R.string.error))
        .setMessage(getString(R.string.something_went_wrong))
        .setPositiveButton(getString(R.string.ok)) { _, _ -> }
        .show()

fun Context.showConfirmationAlert(action: (DialogInterface, Int) -> Unit): AlertDialog =
    MaterialAlertDialogBuilder(this, R.style.Alert)
        .setTitle(getString(R.string.confirmation))
        .setMessage(getString(R.string.are_you_sure))
        .setPositiveButton(getString(R.string.ok), action)
        .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
        .show()
