package com.matrix.autoreply.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.matrix.autoreply.databinding.ItemReplyTemplateBinding

data class ReplyTemplate(val text: String)

class ReplyTemplateAdapter(
    private val templates: List<ReplyTemplate>,
    private val onTemplateClick: (String) -> Unit
) : RecyclerView.Adapter<ReplyTemplateAdapter.TemplateViewHolder>() {

    inner class TemplateViewHolder(private val binding: ItemReplyTemplateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(template: ReplyTemplate) {
            binding.templateText.text = template.text
            binding.useTemplateButton.setOnClickListener {
                onTemplateClick(template.text)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val binding = ItemReplyTemplateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TemplateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        holder.bind(templates[position])
    }

    override fun getItemCount(): Int = templates.size

    companion object {
        /**
         * Default quick reply templates
         */
        fun getDefaultTemplates(): List<ReplyTemplate> {
            return listOf(
                ReplyTemplate("I'm busy right now, I'll get back to you soon."),
                ReplyTemplate("Thanks for your message! I'll respond when I can."),
                ReplyTemplate("I'm currently unavailable. I'll reply as soon as possible."),
                ReplyTemplate("Can't talk now. What's up?"),
                ReplyTemplate("Thanks! 👍")
            )
        }
    }
}
