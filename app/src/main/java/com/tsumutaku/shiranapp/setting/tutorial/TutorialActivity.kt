package com.tsumutaku.shiranapp.setting.tutorial

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import com.stephentuso.welcome.*
import com.tsumutaku.shiranapp.R

class TutorialActivity : WelcomeActivity() {

    companion object {
        /**
         * まだ表示していなかったらチュートリアルを表示
         * SharedPreferencesの管理に関しては内部でよしなにやってくれているので普通に呼ぶだけで良い
         */
        fun showIfNeeded(activity: Activity, savedInstanceState: Bundle?) {
            WelcomeHelper(activity, TutorialActivity::class.java).show(savedInstanceState)
        }

        /**
         * 強制的にチュートリアルを表示したい時にはこちらを呼ぶ
         */
        fun showForcibly(activity: Activity) {
            WelcomeHelper(activity, TutorialActivity::class.java).forceShow()
        }
    }

    /**
     * 表示するチュートリアル画面を定義する
     */

    override fun configuration(): WelcomeConfiguration {

        return WelcomeConfiguration.Builder(this)
            .defaultBackgroundColor(BackgroundColor(Color.DKGRAY))

            .page(BasicPage(
                R.drawable.shiran_app_icon,
                "ようこそ！！",
                "これは\n「し」習慣を\n「ら」楽に\n「ん」半永久的に続けられるようになる\n「プリ」アプリ\n略して「しらんプリ」です。")
                //.background(BackgroundColor(Color.TRANSPARENT))
            )

            .page(BasicPage(
                R.drawable.self_movie2,
                "毎日・その場で・少しだけ",
                "スマホカメラを使って、好きな運動を「自撮り」することで、習慣づくりをお手伝いします。")//フィットネス・ヨガ・勉強など、あなたにとって望ましい行動を
                //.background(BackgroundColor(Color.CYAN))
            )

            .page(BasicPage(
                R.drawable.survey,
                "習慣化のゴール",
                "ビクトリア大学とロンドン大学の研究によれば、\n「最小で週４回を 50~60日間以上」続ければ、より習慣になりやすいといわれています。\n一緒に目指しましょう。")
                //.background(BackgroundColor(Color.DKGRAY))
            )

                /*
                "{自分の習慣にしたい行動を毎日少しずつ自撮りし、それを継続することを手伝います。\n" +
                        "習慣作りの科学的テクニックとして、スモールステップがあります。いきなり大きなミッションを自分に課すのではなく、\n" +
                        "初めは、ちいさなミッションを毎日こなしていき、身体に「クセ」がついてから、徐々に大きなミッションへと成長させていきます。\\n\n" +
                        "例）　今日から１日１０ｋｍ走る!! ➡ ✖\n" +
                        "１日１回　玄関から出て１０歩あるく　➡　◎\\n大事なのは死ぬほど疲れた時でさえ続けられることです。}"
                 */
                //

            .swipeToDismiss(true)
            .build()
    }


}