package com.erenavci.testapplication.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * A full-screen overlay view that renders an animated confetti/firework burst.
 * Add to the window's decor view, call burst(), then remove when onEnd fires.
 */
class ConfettiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val particles = mutableListOf<Particle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var animator: ValueAnimator? = null
    private var onAnimationEnd: (() -> Unit)? = null
    private var progress = 0f

    private val colors = intArrayOf(
        0xFFE91E63.toInt(), // pink
        0xFF9C27B0.toInt(), // purple
        0xFF2196F3.toInt(), // blue
        0xFF4CAF50.toInt(), // green
        0xFFFF9800.toInt(), // orange
        0xFFFFEB3B.toInt(), // yellow
        0xFFF44336.toInt(), // red
        0xFF00BCD4.toInt(), // cyan
        0xFF8BC34A.toInt(), // light green
        0xFFFF5722.toInt(), // deep orange
        0xFFFFFFFF.toInt(), // white sparkle
        0xFFFFD700.toInt(), // gold
    )

    private data class Particle(
        val startX: Float,
        val startY: Float,
        val vx: Float,        // total horizontal displacement at t=1
        val vy: Float,        // total vertical displacement at t=1 (before gravity)
        val gravity: Float,   // downward displacement added as gravity * t²
        val color: Int,
        val size: Float,
        var rotation: Float,
        val rotationSpeed: Float,
        val isRect: Boolean,
        val trail: Boolean    // sparkle/trail particle
    )

    fun burst(originX: Float, originY: Float, onEnd: () -> Unit) {
        onAnimationEnd = onEnd
        particles.clear()

        // Primary burst — 70 particles flying in all directions
        repeat(70) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 280f + 80f  // 80–360 px travel at t=1
            particles.add(
                Particle(
                    startX = originX,
                    startY = originY,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 60f,       // slight upward bias
                    gravity = Random.nextFloat() * 320f + 180f,
                    color = colors[Random.nextInt(colors.size)],
                    size = Random.nextFloat() * 18f + 7f,
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = (Random.nextFloat() - 0.5f) * 14f,
                    isRect = Random.nextBoolean(),
                    trail = false
                )
            )
        }

        // Sparkles — 30 small fast particles for the firework "glitter" effect
        repeat(30) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 400f + 150f
            particles.add(
                Particle(
                    startX = originX,
                    startY = originY,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 120f,
                    gravity = Random.nextFloat() * 500f + 250f,
                    color = colors[Random.nextInt(colors.size)],
                    size = Random.nextFloat() * 7f + 3f,
                    rotation = 0f,
                    rotationSpeed = 0f,
                    isRect = false,
                    trail = true
                )
            )
        }

        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 800L
            interpolator = DecelerateInterpolator(1.2f)
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onAnimationEnd?.invoke()
                }
            })
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val t = progress
        val alpha = ((1f - t) * 255f).toInt().coerceIn(0, 255)

        for (p in particles) {
            // Position: classic projectile motion from start
            val px = p.startX + p.vx * t
            val py = p.startY + p.vy * t + p.gravity * t * t

            // Sparkle trails fade earlier
            val particleAlpha = if (p.trail) ((1f - t * 1.4f).coerceIn(0f, 1f) * 255f).toInt()
                                 else alpha

            paint.color = p.color
            paint.alpha = particleAlpha

            canvas.save()
            canvas.translate(px, py)
            canvas.rotate(p.rotation + p.rotationSpeed * t * 50f)

            if (p.isRect) {
                canvas.drawRect(-p.size / 2f, -p.size / 3f, p.size / 2f, p.size / 3f, paint)
            } else {
                canvas.drawCircle(0f, 0f, p.size / 2f, paint)
            }

            canvas.restore()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}
