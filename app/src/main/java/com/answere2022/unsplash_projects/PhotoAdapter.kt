package com.answere2022.unsplash_projects

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.answere2022.unsplash_projects.data.models.PhotoResponse
import com.answere2022.unsplash_projects.databinding.ItemPhotoBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions


class PhotoAdapter : RecyclerView.Adapter<PhotoAdapter.ViewHolder>() {

    var photos: List<PhotoResponse> = emptyList()

    var onClickPhoto: (PhotoResponse) -> Unit = {}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount(): Int = photos.size

    inner class ViewHolder(
        private var binding: ItemPhotoBinding

    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener{
                onClickPhoto(photos[adapterPosition])
                //클릭당시의 어뎁터포지션의 포토를 이벤트로 전달


            }


        }


        fun bind(photo: PhotoResponse) {

            val dimensionRatio = photo.height / photo.width.toFloat()
            val targetWidth = binding.root.resources.displayMetrics.widthPixels -
                    (binding.root.paddingStart + binding.root.paddingEnd)
            val targetHeight = (targetWidth * dimensionRatio).toInt()


            binding.contentContainer.layoutParams =
                binding.contentContainer.layoutParams.apply {

                    height = targetHeight
                }


            Glide.with(binding.root)
                .load(photo.urls?.regular)
                .thumbnail(
                    Glide.with(binding.root)
                        .load(photo.urls?.thumb)
                        .transition(DrawableTransitionOptions.withCrossFade())

                )
                .override(targetWidth, targetHeight)
                .into(binding.photoImageView)

            Glide.with(binding.root)
                .load(photo.user?.profileImageUrls?.small)
                .placeholder(R.drawable.shape_profile_placeholder)
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.profileImageView)

            if(photo.user?.name.isNullOrBlank()) {
                binding.authorTextView.visibility = View.GONE
            } else {
                binding.authorTextView.visibility = View.VISIBLE
                binding.authorTextView.text = photo.user?.name
            }

            if(photo.description.isNullOrBlank()) {
                binding.descriptionTextView.visibility = View.GONE
            } else {
                binding.descriptionTextView.visibility = View.VISIBLE
                binding.descriptionTextView.text = photo.description
            }




        }

    }
}