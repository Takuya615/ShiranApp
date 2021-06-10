package com.tsumutaku.shiranapp.setting

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class AlertDialogs  : DialogFragment(){

    var title = "title"
    var message = "message"
    var okText = "OK"
    var onOkClickListener : DialogInterface.OnClickListener? = null
    var canselText = "キャンセル"
    var onCanselClickListener : DialogInterface.OnClickListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())//AlertDialog.Builder(context!!)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(okText, onOkClickListener)
            .setNegativeButton(canselText,onCanselClickListener)

        return builder.create()
    }

    override fun onPause() {
        super.onPause()
        // ダイアログを閉じる
        dismiss()
    }
}
/*呼び出し方（例）

class MainActivity:  AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 省略

        var dialog = AlertDialog()
        dialog.title = "更新しました"
        dialog.onOkClickListener = DialogInterface.OnClickListener { dialog, which ->
            finish()
        }
            dialog.show(supportFragmentManager, null)
        }
    }
}

 */